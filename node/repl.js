
process.env.NODE_DISABLE_COLORS = true;

var net = require("net"),
    repl = require("repl");


repl.start("node> ");
net.createServer(function (socket) {
  repl.start("", socket);
}).listen(5001);
