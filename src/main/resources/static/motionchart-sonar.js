function motion_chart_query(url) {
  $('mc_loading').show();
  $('motion_chart').hide();
  $('no_data').hide();
  if ($F('mc_components')!=null) {
    url += '&components=' + $F('mc_components');
  }
  var query = new google.visualization.Query(url);
  query.send(motion_chart_query_callback);
} 

function motion_chart_query_callback(response) {
  $('mc_loading').hide();
  if (response.isError()) {
    error(response.getDetailedMessage());
    return; 
  }
  if (response.getDataTable().getNumberOfRows() > 0) {
    $('motion_chart').show();
    render_motion_chart(response.getDataTable());
  } else {
    $('no_data').show();
  }
}

function render_motion_chart(data_table) {
  var chart = new google.visualization.MotionChart(document.getElementById('motion_chart'));
  var options = {};
  options['width'] = $('motion_chart').getOffsetParent().getWidth() - 50;
  options['height'] = $('motion_chart').getOffsetParent().getHeight() - 50;
  options['showAdvancedPanel'] = false;
  chart.draw(data_table, options);
}