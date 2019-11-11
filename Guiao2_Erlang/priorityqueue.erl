-module(priorityqueue).
-export([create/0, enqueue/2, dequeue/1]).

create() ->
	[].

enqueue({Task, Priority}, []) ->
	if
		not is_number(Priority) -> invalid_element;
		true -> [{Task, Priority}]
	end;

enqueue({Task, Priority}, [{HeadTask, HeadPriority}| Tail]) ->
	if
		not is_number(Priority) -> invalid_element;
		Priority < HeadPriority -> [{HeadTask, HeadPriority}| enqueue({Task, Priority}, Tail)];
		true -> [{Task, Priority}, {HeadTask, HeadPriority}| Tail]
	end.

dequeue([Head| Tail]) ->
	{Tail, Head}.