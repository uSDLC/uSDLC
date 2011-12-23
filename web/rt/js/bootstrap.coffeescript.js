(function() {
  var url;
  if (!window.usdlc) {
    url = window.location;
    document.cookie = "currentPage=" + url.pathname;
    url.href = "" + url.protocol + "//" + url.host + "?" + url.pathname;
  }
}).call(this);
