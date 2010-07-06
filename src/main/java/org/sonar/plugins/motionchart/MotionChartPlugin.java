/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
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

import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

import java.util.Arrays;
import java.util.List;

@Properties({
    @Property(
        key = MotionChartPage.WIDTH_KEY,
        name = "Width",
        description = "Chart width in pixels.",
        defaultValue = MotionChartPage.DEFAULT_WIDTH),
    @Property(
        key = MotionChartPage.HEIGHT_KEY,
        name = "Height",
        description = "Chart height in pixels.",
        defaultValue = MotionChartPage.DEFAULT_HEIGHT),
    @Property(
        key = MotionChartPage.DEFAULT_METRICS_KEY,
        name = "Default axis metrics",
        description = "Comma-separated list of the 4 axis metrics loaded by default : X, Y, color, size.",
        defaultValue = MotionChartPage.DEFAULT_METRICS_VALUE),
    @Property(
        key = MotionChartPage.ADDITIONAL_METRICS_KEY,
        name = "Additional metrics",
        description = "Additional metrics which can be selected as axis.",
        defaultValue = MotionChartPage.ADDITIONAL_METRICS_DEFAULT_VALUE)
})
public class MotionChartPlugin implements Plugin {

  public String getKey() {
    return "motionchart";
  }

  public String getName() {
    return "Motion chart";
  }

  public String getDescription() {
    return "Motion chart";
  }

  public List getExtensions() {
    return Arrays.asList(MotionChartPage.class, MotionChartWebService.class);
  }

  @Override
  public String toString() {
    return getKey();
  }
}
