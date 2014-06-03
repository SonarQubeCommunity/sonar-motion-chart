function loadMotionChart(widgetId, queryString, initOnLastDate) {
  $j('#motion-chart-loading-' + widgetId).show();
  $j('#motion-chart-nodata-' + widgetId).hide();

  var period = $j('#motion-chart-form-' + widgetId).find('select[name="period"]').val();
  queryString += '&period=' + period;

  var components = $j('#motion-chart-form-' + widgetId).find('input[name="components"]:checked');
  if (components.length>0) {
    queryString += '&components=true';
  }
  var query = new google.visualization.Query(baseUrl + '/motion_chart?' + queryString);
  query.send(function (response) {
    $j('#motion-chart-loading-' + widgetId).hide();
    if (response.isError()) {
      error(response.getDetailedMessage());
      return;
    }
    if (response.getDataTable().getNumberOfRows() > 0) {
      $j('#motion-chart-' + widgetId).show();
      drawMotionChart(response.getDataTable(), widgetId, initOnLastDate);
    } else {
      $j('#motion-chart-' + widgetId).hide();
      $j('#motion-chart-nodata-' + widgetId).show();
    }
  });
}

function drawMotionChart(data_table, widgetId, initOnLastDate) {
  var chart = new google.visualization.MotionChart(document.getElementById('motion-chart-' + widgetId));
  var options = {};
  if (initOnLastDate) {
    var allDates=data_table.getDistinctValues(1);
    var latestDate = allDates[allDates.length -1];    
    var year = latestDate.getFullYear();  
    var month = latestDate.getMonth() + 1;
    if (month < 10) {
      month = "0" + month;
    }
    var day = latestDate.getDate();
    options['state'] = '{"time": "' + year + '-' + month + '-' + day + '"}';
  }
  options['width'] = $j('#motion-chart-' + widgetId).width() - 10;
  options['height'] = $j('#motion-chart-' + widgetId).height() - 10;
  chart.draw(data_table, options);
}
