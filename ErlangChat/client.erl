-module (client).
-export ([client/1, sendData/2]).

client(Port) ->
		{ok, Sock} = gen_tcp:connect("127.0.0.1", Port, [binary, {active,false}]),
		io:format("Entrei no servidor ~n"),
		spawn(fun() -> receiveData(Sock) end),
		sendData(Port, Sock).

sendData(Port, Sock) ->
	    Term = io:get_line("message: "),
		ok = gen_tcp:send(Sock, Term),
	    sendData(Port, Sock).


receiveData(Sock) ->
		{ok, Term} = gen_tcp:recv(Sock, 0),
	    io:format("~s~n", [Term]),
	    receiveData(Sock).