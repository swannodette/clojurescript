process.env.NODE_DISABLE_COLORS = true;

var net = require("net"),
    repl = require("repl"),
    vm = require("vm"),
    context = vm.createContext(),
    bootstrap = require("./bootstrap.js");

context.cljs = bootstrap.cljs;
context.goog = bootstrap.goog;

net.createServer(function (socket) {
  var buffer = "";
  socket.setEncoding("utf8");
  socket.on("data", function(data) {
    if(data[data.length-1] != "\0") {
      buffer += data;
    } else {
      if(buffer.length > 0) {
        data = buffer + data;
        buffer = "";
      }
      var ret;
      if(data) {
        data = data.substring(0, data.length-1);
        try {
          ret = vm.runInContext(data, context, "repl");
        } catch (x) {
          console.log(x.stack);
          socket.write(x.stack+"\0");
        }
      }
      if(ret !== undefined && ret !== null) {
        socket.write(ret.toString()+"\0");        
      } else {
        socket.write("nil\0");
      }
    }
  });
}).listen(5001);
