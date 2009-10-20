#
# Sonar, open source software quality management tool.
# Copyright (C) 2009 SonarSource SA
# mailto:contact AT sonarsource DOT com
#
# Sonar is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Sonar is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Sonar; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
#
class Api::MotionchartWebServiceController < Api::GwpResourcesController

  private

  MAX_IN_ELEMENTS=990
  EMPTY_HASH={}

  def rest_call
    @metrics=Metric.by_keys(params[:metrics].split(','))

    period_in_months=(params[:period] || 3).to_i
    min_date=Date.today()<<period_in_months

    #
    # results are limited to 30 snapshots per resource
    # 1 month => 1 snapshot per day
    # 3 months => 1 snapshot every 3 days
    # 1 year => 1 snapshot every 12 days (approximation !)
    #
    @date_interval_in_days=period_in_months

    snapshots=[]
    if @resource
      @display_only_lifetime=true
      # security is already checked by ResourceRestController
      if params[:components]=='true'
        snapshots=Snapshot.find_by_sql(
          ['SELECT s1.id,s1.project_id,s1.created_at FROM snapshots s1,snapshots s2 WHERE s1.parent_snapshot_id=s2.id AND s1.status=? AND s2.project_id=? AND s2.status=? AND s2.created_at>=? ORDER BY s1.created_at desc',
          Snapshot::STATUS_PROCESSED, @resource.id, Snapshot::STATUS_PROCESSED, min_date])
      else
        snapshots=Snapshot.find_by_sql(
          ['SELECT s.id,s.project_id,s.created_at FROM snapshots s WHERE s.project_id=? AND s.status=? AND s.created_at>=? ORDER BY s.created_at desc',
          @resource.id, Snapshot::STATUS_PROCESSED, min_date])
      end

    else
      @display_only_lifetime=false
      # top level projects
      snapshots=Snapshot.find(:all,
        :select => 'snapshots.id,snapshots.project_id,snapshots.created_at',
        :conditions => ['scope=? AND qualifier=? AND status=? AND created_at>=?', Snapshot::SCOPE_SET, Snapshot::QUALIFIER_PROJECT, Snapshot::STATUS_PROCESSED, min_date],
        :order => 'snapshots.created_at DESC')
    end
    rows=(snapshots.empty? ? [] : load_rows(snapshots))

    datatable=load_datatable(rows)

    render :json => jsonp(rest_gwp_ok(datatable))
  end




  #-------------------------------------------------------------
  #
  # Generate Google Wire Format
  #
  #-------------------------------------------------------------

  def load_datatable(rows)
    datatable = {:cols => [], :rows => []}
    add_cols(datatable)
    rows.each do |row|
      add_row(datatable, row)
    end
    datatable
  end

  def add_cols(data_table)
    add_column(data_table, 'r', '', TYPE_STRING)
    add_column(data_table, 'd', 'Date', TYPE_DATE_TIME)
    @metrics.each do |metric|
      add_column(data_table, metric.key, metric.short_name, TYPE_NUMBER)
    end
  end

  def add_row(datatable, datarow)
    row = new_row(datatable)
    add_row_value(row, datarow[2].name)
    add_row_value(row, Api::GwpJsonDate.new(datarow[1]))
    for index in 3...datarow.size do
      add_row_value(row, datarow[index])
    end
  end

  def add_row_value(row, value, formatted_value = nil)
    if value
      if formatted_value
        row[:c] << {:v => value, :f => formatted_value}
      else
        row[:c] << {:v => value}
      end
    else
      row[:c] << EMPTY_HASH
    end
  end


  #-------------------------------------------------------------
  #
  # Select dates to display
  #
  #-------------------------------------------------------------

  def reference_dates(min_date, max_date, interval_in_days)
    dates=[]
    max_date.to_date.step(min_date.to_date, -interval_in_days) do |d|
      dates<<d
    end
    dates
  end




  #-------------------------------------------------------------
  #
  # Load data
  #
  #-------------------------------------------------------------

  # snapshots: descending sort
  def load_rows(snapshots)
    # dates: descending sort
    dates=reference_dates(snapshots[-1].created_at, snapshots[0].created_at, @date_interval_in_days)

    # snapshots_per_resource_id values are sorted by date (descending)
    snapshots_per_resource_id={}
    snapshots.each do |snapshot|
      snapshots_per_resource_id[snapshot.project_id]||=[]
      snapshots_per_resource_id[snapshot.project_id]<<snapshot
    end

    rows=[]
    snapshots_per_resource_id.each_value do |resource_snapshots|
      rows.concat(build_rows(resource_snapshots, dates))
    end

    fill_resources(rows)
    fill_measures(rows)
    rows
  end




  def build_rows(snapshots, dates)
    index_snapshot=0
    current_snapshot=snapshots[index_snapshot]
    max_date=current_snapshot.created_at.to_date

    result=[]
    dates.each do |date|
      while current_snapshot && current_snapshot.created_at.to_date>=date
        index_snapshot+=1
        current_snapshot=(index_snapshot>=snapshots.size ? nil : snapshots[index_snapshot])
      end
      if current_snapshot && (!@display_only_lifetime || date<=max_date)
        result<<build_row(current_snapshot, date)
      end
    end
    result
  end

  # a row is an array of [snapshot, date, resource, measures...]
  def build_row(snapshot, date)
    row=Array.new(2+@metrics.size)
    row[0]=snapshot
    row[1]=date
    row
  end

  def fill_resources(rows)
    rids=rows.collect{|row| row[0].project_id}.uniq.compact

    # split IN clause in maximum 990 elements (bug with Oracle)
    resources=[]
    loops = rids.length / MAX_IN_ELEMENTS
    loops += 1 if rids.length % MAX_IN_ELEMENTS > 0
    loops.times do |i|
      start_index = i * MAX_IN_ELEMENTS
      end_index = (i+1) * MAX_IN_ELEMENTS
      resources.concat(Project.find(:all, :conditions => {:id => rids[start_index...end_index]}))
    end
    resource_per_id={}
    resources.each do |resource|
      resource_per_id[resource.id]=resource
    end

    rows.each do |row|
      row[2]=resource_per_id[row[0].project_id]
    end

    rows
  end

  def fill_measures(rows)
    # potential bug with Oracle (IN elements >= 1000)
    sids=rows.collect{|row| row[0].id}
    mids=@metrics.select{|m| m.id}
    measures=[]
    loops = sids.length / MAX_IN_ELEMENTS
    loops += 1 if sids.length % MAX_IN_ELEMENTS > 0
    loops.times do |i|
      start_index = i * MAX_IN_ELEMENTS
      end_index = (i+1) * MAX_IN_ELEMENTS
      measures.concat(ProjectMeasure.find(:all,
          :select => 'project_measures.value,project_measures.metric_id,project_measures.snapshot_id',
          :conditions => ['rules_category_id IS NULL AND rule_id IS NULL AND rule_priority IS NULL AND metric_id IN (?) AND snapshot_id IN (?)',
            mids, sids[start_index...end_index]]))
    end


    metric_index_per_id={}
    for i in 0...@metrics.size do
      metric_index_per_id[@metrics[i].id]=3+i
    end

    rows_per_sid={}
    rows.each do |row|
      rows_per_sid[row[0].id]||=[]
      rows_per_sid[row[0].id]<<row
    end

    measures.each do |measure|
      if measure.value
        snapshot_rows=rows_per_sid[measure.snapshot_id]
        column_index=metric_index_per_id[measure.metric_id]
        value=((measure.value*100).to_i)/100.0
        snapshot_rows.each do |row|
          row[column_index]=value
        end
      end
    end

    rows
  end
end