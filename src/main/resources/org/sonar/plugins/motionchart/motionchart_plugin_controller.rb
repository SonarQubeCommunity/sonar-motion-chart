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
class Api::RubyMotionchartWebServiceController < Api::GwpResourcesController

  private
  
  def rest_call
    metrics=Metric.by_keys(params[:metrics].split(','))
    
    if @resource
      last_snapshot=@resource.last_snapshot
      snapshots=Snapshot.find(:all, :conditions => {:project_id => @resource.id, :status => Snapshot::STATUS_PROCESSED}, :order => 'created_at')
      # get all the first level child snapshots for this parent
      child_snapshots=Snapshot.find(:all, :conditions => 
          ['parent_snapshot_id IN (?)', snapshots.map{|s| s.id}], :include => 'project')

    else
      # history for top level projects
      snapshots = Snapshot.last_authorized_enabled_projects(current_user)

      # get all the first level child snapshots for the snapshots resource ids
      child_snapshots=Snapshot.find(:all, :conditions => 
          ['project_id IN (?) AND status=?', snapshots.map{|s| s.project.id}, Snapshot::STATUS_PROCESSED], :include => 'project')
    end

    # temporary fix for SONAR-1098
    if child_snapshots.length > 999
      loops_count = child_snapshots.length / 999
      loops_count = loops_count + 1 if child_snapshots.length % 999 > 0
      measures = []
      loops_count.times do |i|
        start_index = i * 999
        end_index = (i+1) * 999
        measures.concat(get_measures(metrics, child_snapshots[start_index...end_index]))
      end
    else
      measures = get_measures(metrics, child_snapshots)
    end
    
    snapshots_measures_by_resource = {}

    # ---------- SORT RESOURCES
    if not measures.empty?  
      measures_by_sid = {}
      measures.each do |measure|
        measures_by_sid[measure.snapshot_id]||=[]
        measures_by_sid[measure.snapshot_id]<<measure
      end
      
      snapshots_by_resource={}
      child_snapshots.each do |snapshot|
        snapshots_by_resource[snapshot.project]||=[]
        snapshots_by_resource[snapshot.project]<<snapshot
      end
      
      snapshots_by_resource.each_pair do |resource, snapshots|
        snapshots_measures = {}
        snapshots.each do |snapshot|
          measures_by_metrics = {}
          measures = measures_by_sid[snapshot.id] || []
          measures.each do |measure|
            measures_by_metrics[measure.metric_id] = measure
          end
          snapshots_measures[snapshot] = measures_by_metrics if not measures.empty?
        end
        snapshots_measures_by_resource[resource] = snapshots_measures
      end

    end
    # ---------- FORMAT RESPONSE
    rest_render({ :metrics => metrics, :snapshots_measures_by_resource => snapshots_measures_by_resource, :params => params})
  end
  
  def select_columns_for_measures
    'project_measures.id,project_measures.value,project_measures.metric_id,project_measures.snapshot_id'
  end
  
  def get_measures(metrics, child_snapshots)
    ProjectMeasure.find(:all,
          :select => select_columns_for_measures,
          :conditions => ['rules_category_id IS NULL and rule_id IS NULL and rule_priority IS NULL and metric_id IN (?) and snapshot_id IN (?)',
            metrics.select{|m| m.id}, child_snapshots.map{|s| s.id}], :order => "project_measures.value")
  end
  
  def fill_gwp_data_table(objects, data_table)
    metrics = objects[:metrics]
    add_cols(data_table, metrics)
    
    objects[:snapshots_measures_by_resource].each_pair do |resource, snapshots_measures|
      snapshots_measures.each_pair do |snapshot, measures_by_metrics|
        measures_for_row(data_table, resource, snapshot, measures_by_metrics, metrics)
      end
    end
  end

  def add_cols(data_table, metrics)
    add_column(data_table, 'r', 'Resource', TYPE_STRING)
    add_column(data_table, 'd', 'Date', TYPE_DATE_TIME)
    metrics.each do |metric|
      add_column(data_table, metric.key, metric.short_name, TYPE_NUMBER)
    end
  end
  
  def measures_for_row(data_table, resource, snapshot, measures_by_metrics, metrics)
    row = new_row(data_table)
    add_row_value(row, resource.fullname)
    add_row_value(row, Api::GwpJsonTime.new(snapshot.created_at))
   
    metrics.each do |metric|
      measure = measures_by_metrics[metric.id]
      add_row_value(row, (measure.nil? ? nil : measure.value))
    end
  end

end