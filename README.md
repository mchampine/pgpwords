# pgpwords

Convert number sequences to [pgpwords](https://en.wikipedia.org/wiki/PGP_word_list) sequences and back.

Useful for authentication over a voice channel

## Usage

Generate int sequence with SecureRandom

    (rnd-ints 5)
    ;;=> (250 38 111 85 18)

Convert to pgpgwords

	(hex->words *1)
	;;=> ("wallet" "caretaker" "gremlin" "equipment" "atlas")

Convert pgpwords to a long

	(words->long *1)
	;;=> 653686720632

Convert long to pgpwords

	(long->words *1)
	("printer" "component" "snowcap" "retrieval" "island")

Generate random pgpwords sequence
	
	(pgpwords-gen 5)
	;;=> ("offload" "politeness" "crumpled" "quantity" "stormy")

Generate secure random pgpwords sequence

	(pgpwords-sr-gen 5)
	;;=> ("glucose" "retrospect" "skullcap" "trombonist" "prefer")

Generate random challenge/response pairs

	(crgen)
	;;=> {:challenge {:i 4792, :w ("atlas" "provincial")},
	;;=>  :response {:i 8216095, :w ("klaxon" "finicky" "billiard")}}

Generate one-use hotp tokens

	(use-one-hotp-token)
	;;=> {:tok "0ef61a", :words ("apple" "vocalist" "beehive")}
	(use-one-hotp-token)
	;;=> {:tok "842abe", :words ("mural" "chambermaid" "skydive")}

Validate pgpwords

	(hex->words srns5)
	;;=> ("printer" "component" "snowcap" "retrieval" "island")

	(validate-and-errors *1 srns5)
	;;=> {:valid true, :errors "No Errors"}

Alter one of the words to invalidate the sequence

	(validate-and-errors '("printer" "component" "snowCap" "retrieval" "island") srns5)
	;;=> {:valid false, :errors "First error is word: snowCap, at position: 3"}

Generate a passphrase

	(pwgen 3)
	;;=> {:rndint 11456474, :phrase ("robust" "Saturday" "surmount")}
	(pwgen 4)
	;;=> {:rndint 1726672839, :phrase ("framework" "undaunted" "uncut" "retraction")}

## License

Copyright © 2016 Mark Champine

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
