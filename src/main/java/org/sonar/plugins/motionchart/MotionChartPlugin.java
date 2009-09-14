/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.motionchart;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.motionchart.client.GwtMotionChart;

@Properties({
  @Property(
      key = GwtMotionChart.WIDTH_PROP,
      name = "Chart width",
      description = "The motion chart width in pixels",
      defaultValue = GwtMotionChart.DEFAULT_WIDTH),
  @Property(
      key = GwtMotionChart.HEIGHT_PROP,
      name = "Chart height",
      description = "The motion chart height in pixels",
      defaultValue = GwtMotionChart.DEFAULT_HEIGHT)
})
public class MotionChartPlugin implements Plugin {

  public String getKey() {
    return "gwt-motionchart";
  }

  public String getName() {
    return "Motion chart";
  }

  public String getDescription() {
    return "GWT metrics history motion chart plugin";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(GwtMotionChartPage.class);
    // for dev to avoid Cross scripting Ajax call issues with Gwt use the Ruby page
    //extensions.add(RubyMotionChartPage.class);
    return extensions;
  }

  @Override
  public String toString() {
    return getKey();
  }
}
