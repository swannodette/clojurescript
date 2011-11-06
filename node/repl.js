process.env.NODE_DISABLE_COLORS = true;

var net = require("net"),
    repl = require("repl"),
    vm = require("vm"),
    context = vm.createContext(),
    bootstrap = require("./bootstrap.js");

context.cljs = bootstrap.cljs;
context.goog = bootstrap.goog;

net.createServer(function (socket) {
  socket.setEncoding("utf8");
  socket.on("data", function(data) {
    var ret;
    data = data.trim();
    if(data) {
      try {
        ret = vm.runInContext(data, context, "repl");
      } catch (x) {
        console.log("Error:", x);
      }
    }
    if(ret != undefined) {
      socket.write(ret.toString()+"\0");
    }
  });
}).listen(5001);
