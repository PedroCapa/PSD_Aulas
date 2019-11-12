-module(server).
-export([server/1]).

server(Port) ->
		Room = spawn(fun()-> room([], []) end),
		{ok, LSock} = gen_tcp:listen(Port, [binary, {packet, 0}]),
		acceptor(LSock, Room).

acceptor(LSock, Room) ->
		{ok, Sock} = gen_tcp:accept(LSock),
		Room ! {new_user, Sock},
		gen_tcp:controlling_process(Sock, Room),
		acceptor(LSock, Room).


room(Sockets, Users) ->
		receive
			{new_user, Sock} ->
				io:format("newuser ~n", []),
				Persons = authentication(Users),
				io:format("authentication made with sucess ~n", []),
				room([Sock | Sockets], Persons);
			{tcp, _, Data} ->
				io:format("received ~p ~n", [binary_to_list(Data)]),
				[gen_tcp:send(Socket, Data) || Socket <- Sockets],
				room(Sockets, Users);
			{tcp_closed, Sock} ->
				io:format("user disconnected ~n", []),
				room(Sockets -- [Sock], Users);
			{tcp_error, Sock, _} ->
				io:format("tcp error ~n", []),
				room(Sockets -- [Sock], Users)
		end.

authentication(Users) ->
		receive
			{tcp, _, Data} ->
				Username = binary_to_list(Data),
				Persons = receivePassword(Username, Users),
				Persons
		end.


receivePassword(Username, Users) ->
		receive
			{tcp, _, Data} ->
				Password = binary_to_list(Data),
				Persons = authentication2(Username, Users, Password),
				Persons
		end.

authentication2(Username, Users, Password) ->		
			CheckUsername = containsUsername(Username, Users),
			CheckPassword = containsPassword(Username, Users, Password),
		if
			CheckUsername, CheckPassword  -> Users;
			CheckUsername, CheckPassword =:= false -> io:format("Entrou muito mal~n", []), authentication(Users);
			CheckUsername =:= false -> createPerson(Username, Users, Password) % talvez meter depois do if
		end.


containsUsername(_, []) -> false;
containsUsername(Username, [{Name, _} | Tail]) ->
		if Username == Name -> true;
			true -> io:format("Comparei um utilizador errado~n", []), containsUsername(Username, Tail)
		end.

containsPassword(_, [], _) -> false;
containsPassword(Username, [{Name, Pass} | Tail], Password) ->
	if Username == Name,  Pass == Password ->
				true;
			Username == Name,  Pass =:= Password ->
				false;
			true ->
				containsUsername(Username, Tail)
	end.

createPerson(Username, Users, Password) -> [{Username, Password} | Users].