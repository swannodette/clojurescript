process.env.NODE_DISABLE_COLORS = true;

var net = require("net"),
    repl = require("repl"),
    vm = require("vm"),
    bootstrap = require("./bootstrap.js"),
    cljs = bootstrap.cljs,
    goog = bootstrap.goog,
    _;

net.createServer(function (socket) {
  socket.setEncoding("utf8");
  socket.on("data", function(data) {
    var ret;
    data = data.trim();
    if(data) {
      try {
        ret = vm.runInThisContext(data);
      } catch (x) {
        console.log("Error:", x);
      }
    }
    console.log(_);
    if(ret != undefined) {
      socket.write(ret.toString()+"\n");
    }
  });
}).listen(5001);
