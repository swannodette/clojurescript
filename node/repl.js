process.env.NODE_DISABLE_COLORS = true;

var net = require("net"),
    repl = require("repl"),
    vm = require("vm"),
    context = vm.createContext();

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
        // not sure how \0's are getting through - David
        data = data.replace(/\0/g, "");
        try {
          //console.log(data);
          ret = vm.runInContext(data, context, "repl");
        } catch (x) {
          //console.log(data);
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
