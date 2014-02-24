/*
 * SonarQube Motion Chart Plugin
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.motionchart;

public interface WidgetConstants {
  String METRIC_X_PROP_KEY = "metric-x";
  String METRIC_X_PROP_DEF_VALUE = "ncloc";

  String METRIC_Y_PROP_KEY = "metric-y";
  String METRIC_Y_PROP_DEF_VALUE = "coverage";

  String METRIC_COLOR_PROP_KEY = "metric-color";
  String METRIC_COLOR_PROP_DEF_VALUE = "duplicated_lines";

  String METRIC_SIZE_PROP_KEY = "metric-size";
  String METRIC_SIZE_PROP_DEF_VALUE = "violations";

  String CHART_HEIGHT_PROP_KEY = "chartHeight";
  String CHART_HEIGHT_PROP_DEF_VALUE = "400";

  String INIT_CHART_ON_LATEST_DATE = "initChartOnLatestDate";
}
