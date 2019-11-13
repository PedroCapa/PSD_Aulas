-module(server).
-export([server/1]).

server(Port) ->
		Room = spawn(fun()-> room([], [], []) end),
		{ok, LSock} = gen_tcp:listen(Port, [binary, {packet, 0}]),
		acceptor(LSock, Room).

acceptor(LSock, Room) ->
		{ok, Sock} = gen_tcp:accept(LSock),
		Room ! {new_user, Sock},
		gen_tcp:controlling_process(Sock, Room),
		acceptor(LSock, Room).


room(Sockets, Users, Messages) ->
		receive
			{new_user, Sock} ->
				io:format("newuser ~n", []),
				Persons = authentication(Users),
				io:format("authentication made with sucess ~n", []),
				[gen_tcp:send(Sock, Data) || Data <- Messages],
				room([Sock | Sockets], Persons, Messages);
			{tcp, _, Data} ->
				io:format("received ~p ~n", [binary_to_list(Data)]),
				[gen_tcp:send(Socket, Data) || Socket <- Sockets],
				room(Sockets, Users, putEndMessage(Data, Messages));
			{tcp_closed, Sock} ->
				io:format("user disconnected ~n", []),
				room(Sockets -- [Sock], Users, Messages);
			{tcp_error, Sock, _} ->
				io:format("tcp error ~n", []),
				room(Sockets -- [Sock], Users, Messages)
		end.

authentication(Users) ->
		receive
			{tcp, _, Data} ->
				Username = binary_to_list(Data)
		end,
		receive
			{tcp, _, Pass} ->
				Password = binary_to_list(Pass),
				Persons = checkPerson(Username, Users, Password),
				Persons
		end.

checkPerson(Username, Users, Password) ->		
			CheckUsername = containsUsername(Username, Users),
			CheckPassword = containsPassword(Username, Users, Password),
		if
			CheckUsername, CheckPassword  -> 
				Users;
			CheckUsername, CheckPassword =:= false -> 
				io:format("Nome de utilizador ou palavra-passe incorretos~n", []), authentication(Users); 
				%EM vez de ser io enviar para o cliente a dizer que entrou mal
			CheckUsername =:= false -> 
				createPerson(Username, Users, Password)
		end.

containsUsername(_, []) -> false;
containsUsername(Username, [{Name, _} | Tail]) ->
		if Username == Name -> true;
			true -> containsUsername(Username, Tail)
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

createPerson(Username, Users, Password) -> 
		[{Username, Password} | Users].


putEndMessage(Data,[]) -> [Data];
putEndMessage(Data, [H | T]) -> [H | putEndMessage(Data, T)].