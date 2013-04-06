App = Ember.Application.create();

App.Store = DS.Store.extend({
  revision: 12
});

DS.RESTAdapter.configure("plurals", {
  entry: "entries"
});

App.Router.map(function() {
  this.resource("feeds");
  this.route("weather", { path: "/weather"});
  this.resource("feed", { path: "/feed/:feed_id" });
  this.resource("entry", { path: "/entry/:entry_id" });
});
