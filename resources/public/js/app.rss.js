App.Feed = DS.Model.extend({
  title: DS.attr("string"),
  description: DS.attr("string"),
  url: DS.attr("string"),
  entries: DS.hasMany("App.Entry"),

  getUpdates: function() {
    var feed = this
    $.getJSON("/feeds/" + this.get("id") + "/updates", function(data) {
      data.forEach(function(e) {
        feed.store.load(App.Entry, e);
        feed.get("entries").pushObject(App.Entry.find(e.id));
        feed.store.get("defaultTransaction").rollback();
      })
    })
  },

  sortedEntries: function() {
    return this.get("entries").toArray().sort(function(a,b) {
      return new Date(b.get("published")) - new Date(a.get("published"));
    });
  }.property("entries.@each.isLoaded"),

  unreadCount: function () {
    var count = 0;
    this.get("entries").forEach(function(e) {
      if (!e.get("read"))
        count++;
    })
    return count;
  }.property("entries.@each.read")
});

App.Entry = DS.Model.extend({
  title: DS.attr("string"),
  description: DS.attr("string"),
  url: DS.attr("string"),
  feed: DS.belongsTo("App.Feed"),
  published: DS.attr("string"),
  read: DS.attr("boolean")
});

App.FeedsController = Ember.ArrayController.extend(Ember.SortableMixin, {
  sortProperties: ["id"],

  create: function(url) {
    App.Feed.createRecord({url: url}).store.commit();
  },

  delete: function(feed) {
    feed.deleteRecord();
    feed.store.commit();
  },

  update: function(feed) {
    feed.getUpdates();
  },

  allUnread: function() {
    var res = [];
    if (this.content)
      this.content.forEach(function(feed) {
        feed.get("entries").forEach(function(entry) {
          if (!entry.get("read"))
            res.push(entry);
        });
      });
    return res;
  }.property("content.@each.isLoaded", "content.@each.unreadCount"),

  readAll: function() {
    this.get("allUnread").forEach(function(e) {
      e.set("read", true);
    });
    this.store.commit();
  }
});

App.EntryRoute = Ember.Route.extend({
  activate: function() {
    var entry = this.get("context");
    entry.set("read", true);
    entry.store.commit();
  },

  model: function(params) {
    return App.Entry.find(params.entry_id);
  }
});

App.FeedRoute = Ember.Route.extend({
  model: function(params) {
    return App.Feed.find(params.feed_id);
  }
});

App.FeedsRoute = Ember.Route.extend({
  setupController: function(controller) {
    controller.set("content", App.Feed.find());
  }
});

App.FeedView = Ember.View.extend({
  readAll: function() {
    this.controller.get("entries").forEach(function(e) {
      e.set("read", true);
    })
    this.controller.store.commit();
  }
});

App.PartialEntryView = Ember.View.extend({
  tagName: "tr",
  classNameBindings: ["read::info"],
  read: function() {
    return this.controller.get("read")
  }.property("controller.read"),
});

App.PartialFeedView = Ember.View.extend({
  unread: function() {
    return this.controller.get("unreadCount") != 0;
  }.property("controller.unreadCount")
});

App.FeedsView = Ember.View.extend({
  updateAll: function() {
    $(".update-feed").click();
  },

  anyUnread: function() {
    return this.controller.get("allUnread").length != 0;
  }.property("controller.allUnread"),

  didInsertElement: function() {
    $("button[data-loading-text]").click(function () {
      var btn = $(this);
      btn.button("loading");
      //TODO: Do not cheat.
      setTimeout(function () {
        btn.button("reset");
      }, 1000)
    })
  }
});
