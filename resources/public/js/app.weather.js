App.Plottable = Ember.Mixin.create({
  plot: function (name, forecast) {
    var temp = [];
    var time = [];

    forecast.forEach(function(e) {
      temp.push(Math.round((e.main.temp - 273.15) * 100) / 100);
      time.push(new Date(e.dt * 1000));
    })

    chart = new Highcharts.Chart({
      chart: {
	renderTo: name,
	type: "spline"
      },
      title: { text: "Temperature during two days" },
      yAxis: { title: { text: "Temperature" }},
      xAxis: {
	type: "datetime",
	categories: time,
	tickInterval: 8,
	labels: {
	  formatter: function() {
	    return Highcharts.dateFormat("%H:00", this.value);
	  }
	}
      },
      series: [{
	name: "Temperature",
	type: "spline",
	data: temp
      }]
    });
  }
})

App.Weather = Ember.Object.extend(App.Plottable, {
  city_id: null,
  current: null,
  history: null,

  getCurrent: function() {
    var weather = this;

    $.getJSON("http://freegeoip.net/json/", function(data) {
          var url = "http://api.openweathermap.org/data/2.1/find/name?q="
        + encodeURIComponent(data.city)
        + ",%20"
        + data.country_code
        + "&units=metric";

      $.getJSON(url + "&callback=?", function(data) {
        weather.set("city_id", data.list[0].id);
        weather.set("current", data.list[0]);
      })
    });

    return weather;
  },

  getHistory: function() {
    var city_id = this.get("city_id");
    var plot = this.get("plot");

    var url = "http://api.openweathermap.org/data/2.1/history/city?id="
        + city_id
        + "&units=metric&cnt=50"

    $.getJSON(url + "&callback=?", function(data) {
      plot("plot", data.list);
    })
  }.observes("city_id")
});

App.WeatherRoute = Ember.Route.extend({
  model: function() {
    return App.Weather.create().getCurrent();
  }
});
