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
    metrics=Metric.by_keys(params[:metrics].split(','))

    snapshots=[]
    if @resource
      # security is already checked by ResourceRestController
      snapshots=Snapshot.find_by_sql(['SELECT s1.id,s1.project_id,s1.created_at FROM snapshots s1,snapshots s2 WHERE s1.parent_snapshot_id=s2.id AND s1.status=? AND s2.project_id=? AND s2.status=?', Snapshot::STATUS_PROCESSED, @resource.id, Snapshot::STATUS_PROCESSED])
      snapshots=compact(snapshots)
    else
      # top level projects
      snapshots=Snapshot.find(:all,
        :select => 'snapshots.id,snapshots.project_id,snapshots.created_at',
        :conditions => {:scope => 'PRJ', :qualifier => 'TRK', :status => Snapshot::STATUS_PROCESSED})
      snapshots=filter_all_projects(snapshots)
    end

    load_resources(snapshots)
    measures=load_measures(snapshots,metrics)
    rows=load_rows(snapshots,measures,metrics)
    datatable=load_datatable(rows,metrics)

    render :json => jsonp(rest_gwp_ok(datatable))
  end

  # avoid the N+1 requests syndrom
  def load_resources(snapshots)
    rids=snapshots.collect{|s| s.project_id}.uniq

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

    snapshots.each do |snapshot|
      snapshot.project=resource_per_id[snapshot.project_id]
    end
    snapshots
  end

  def load_datatable(rows,metrics)
    datatable = {:cols => [], :rows => []}
    add_cols(datatable,metrics)
    rows.each do |row|
      add_row(datatable, row)
    end
    datatable
  end

  def load_measures(snapshots,metrics)
    # potential bug with Oracle (IN elements >= 1000)
    sids=snapshots.map{|s| s.id}
    mids=metrics.select{|m| m.id}
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
    measures
  end

  # rows are array of row. A row is a array of fields :
  # [resource name, date, value1, value2, value3, value4]
  def load_rows(snapshots, measures, metrics)
    metric_index_per_id={}
    for i in 0...metrics.size do
      metric_index_per_id[metrics[i].id]=2+i
    end

    rows=[]
    row_per_sid={}
    snapshots.each do |snapshot|
      row=Array.new(2+metrics.size)
      row[0]=snapshot.project.fullname
      row[1]=snapshot.created_at
      rows<<row
      row_per_sid[snapshot.id]=row
    end

    measures.each do |measure|
      if measure.value
        row=row_per_sid[measure.snapshot_id]
        row[metric_index_per_id[measure.metric_id]]=((measure.value*100).to_i)/100.0
      end
    end
    rows
  end

  def add_cols(data_table, metrics)
    add_column(data_table, 'r', '', TYPE_STRING)
    add_column(data_table, 'd', 'Date', TYPE_DATE_TIME)
    metrics.each do |metric|
      add_column(data_table, metric.key, metric.short_name, TYPE_NUMBER)
    end
  end

  def add_row(datatable, datarow)
    row = new_row(datatable)
    add_row_value(row, datarow[0])
    add_row_value(row, Api::GwpJsonTime.new(datarow[1]))
    for index in 2...datarow.size do
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

  def filter_all_projects(snapshots)
    snapshots_per_resource_id={}
    snapshots.each do |snapshot|
      snapshots_per_resource_id[snapshot.project_id]||=[]
      snapshots_per_resource_id[snapshot.project_id]<<snapshot
    end

    result=[]
    snapshots_per_resource_id.each_pair do |resource_id, snapshots|
      result.concat(compact(snapshots))
    end
    result
  end

  def compact(array, max_size=30)
    return array if array.size<=max_size
    # example : array size is 80. The goal is to compact to 30.

    # remove one item on three => (80/30)+1
    frequency=(array.size/max_size)+1
    for i in frequency-1 .. array.size-1 do
      if (i.modulo(frequency)==0 && array.nitems>max_size)
        array[i]=nil
      end
    end

    # remove nil elements
    array.compact!

    compact(array)
  end

end