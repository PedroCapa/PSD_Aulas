-module(serverSocket).
-export([server/1]).

server(Port) ->
		Room = spawn(fun()-> room(createChat(), []) end),
		{ok, LSock} = gen_tcp:listen(Port, [binary, {packet, line}, {reuseaddr, true}]),
		acceptor(LSock, Room).

acceptor(LSock, Room) ->
		{ok, Sock} = gen_tcp:accept(LSock),
		spawn(fun() -> acceptor(LSock, Room) end),
		Room ! {enter, self()},
		user(Sock, Room).

room(Pids, Users) ->
		receive
			{enter, Pid} ->
				io:format("userentered ~n", []),
				Pid ! {tcp, "", ""},
				room(putChat("\\room Lobby\n", Pids, Pid), Users);
			{aut, {Username, Password}, PID}  ->
				CheckUsername = containsUsername(Username, Users),
				CheckPassword = containsPassword(Username, Users, Password),
				if
					CheckUsername =:= true, CheckPassword =:= true -> 
						PID ! {sucess, {Username, Password}, "\\room Lobby\n"},
						room(Pids, Users);
					CheckUsername =:= true, CheckPassword =:= false -> 
						PID ! {tcp, "", ""},
						room(Pids, Users);
					CheckUsername =:= false -> 
						PID ! {sucess, {Username, Password}, "\\room Lobby\n"}, 
						room(Pids, [{Username, Password} | Users]) 
				end;
			{line, Data, PID, Sala, Username} ->
				R = containsChat(binary_to_list(Data), Pids),
				if
					 R == true ->
					 	PID ! {sala, Data},
					 	room(changeChat(Data, Pids, PID), Users);
					 true -> 
					 	[sendMessage(P, {line, list_to_binary(Username ++ ": 	" ++ binary_to_list(Data))}) || P <- sameRoom(Sala, Pids)], 
					 	room(Pids, Users)
				end;				
			{leave, Room, Pid} ->
				io:format("userleft ~n", []),
				PID = removeUser(Pids, Pid, Room),
				room(PID, Users)
		end.

sameRoom(_, []) -> [];
sameRoom(Sala, [{Topic, PID} | T ]) ->
		if
			Sala == Topic ->
				PID;
			true -> 
				sameRoom(Sala, T)
		end.

sendMessage(P, Msg) -> P ! Msg.

user(Sock, Room) ->
	receive
		{line, Data} ->
			gen_tcp:send(Sock, Data),
			user(Sock, Room);
		{tcp, _, _} ->
			gen_tcp:send(Sock, "Put the credentials"),
			Person = authentication(),
			Room ! {aut, Person, self()},
			user(Sock, Room);
		{sucess, Person, Sala} -> 
			gen_tcp:send(Sock, "authenticated with sucess"),
			authenticated(Sock, Room, Person, Sala);
		{tcp_closed, _} ->
			Room ! {leave, Room, self()};
		{tcp_error, _, _} ->
			Room ! {leave, Room, self()}
	end.

authentication() ->
		receive
			{tcp, _, Data} ->
				Username = binary_to_list(Data)
		end,
		receive
			{tcp, _, Pass} ->
				Password = binary_to_list(Pass),
				{Username, Password}
		end.

authenticated(Sock, Room, {Username, Password}, Sala) ->
		receive
			{line, Data} ->
				gen_tcp:send(Sock, binary_to_list(Data)),
				authenticated(Sock, Room, {Username, Password}, Sala);
			{tcp, _, Data} ->
				Room ! {line, Data, self(), Sala, Username},
				authenticated(Sock, Room, {Username, Password}, Sala);
			{sala, S} ->
				gen_tcp:send(Sock, "Mudou para a sala " ++ S),
				authenticated(Sock, Room, {Username, Password}, binary_to_list(S));
			{tcp_closed, _} ->
				Room ! {leave, Sala, self()};
			{tcp_error, _, _} ->
				Room ! {leave, Sala, self()}
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

createChat() ->
		[{"\\room Lobby\n", []}, {"\\room Desporto\n", []}, {"\\room Politica\n", []}].

changeChat(Room, Pids, PID) ->
		A = removeChat(Pids, PID),
		B = putChat(binary_to_list(Room), A, PID),
		B.

removeChat([], _) -> [];
removeChat([{Topic, Pids} | T], PID) -> [{Topic, Pids -- [PID]} | removeChat(T, PID)].

putChat(_, [], _) -> [];
putChat(Room, [{Topic, Users} | T], Pid) ->
		if 
			Room == Topic ->
				[{Topic, [Pid | Users]} | T];
			true -> 
				[{Topic, Users} | putChat(Room, T, Pid)]
		end.

containsChat(_, []) -> false;
containsChat(Room, [{Topic, _} | T]) ->
		if Room == Topic ->
			true;
			true -> containsChat(Room, T)
		end.

removeUser([], _, _) -> [];
removeUser([{Topic, Users} | T], Pid, Room) -> 
		if Room == Topic ->
			[{Topic, Users -- [Pid]} | T];
			true -> [{Topic, Users} |removeUser(T, Pid, Room)]
		end.