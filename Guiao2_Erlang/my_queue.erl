-module(my_queue).
-export([create/0, enqueue/2, dequeue/1]).

create() -> [].

enqueue(Element, Queue) ->
	insert_tail(Element, Queue).

insert_tail(Element, []) ->
	[Element];

insert_tail(Element, [Head|Tail]) ->
	[Head|insert_tail(Element, Tail)].

dequeue([]) ->
	empty;

dequeue([Head|Tail]) ->
	{Tail, Head}.