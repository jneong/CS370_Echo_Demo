These patterns are for use with the Intent Utterance Expander, found here:
https://lab.miguelmota.com/intent-utterance-expander/example/

Documentation for utterances can be found here:
https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/supported-phrases-to-begin-a-conversation

````
AllCategoryIntent (give|tell) me (what is|what's) (scheduled|happening)
AllCategoryIntent (give|tell) me (what is|what's) up
AllCategoryIntent (what is|what's) an (event|occasion)
AllCategoryIntent I (want|need) all the (events|occasions)
AllCategoryIntent an (event|occasion)
AllCategoryIntent is (the|a) (scheduled|happening)
AllCategoryIntent to (give|tell) me all the (events|occasions)
ArtsAndEntertainmentCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent (what is|what's) an (event|occasion) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent I (want|need) all the (events|occasions) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent an (event|occasion) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent is (the|a) (for|about) (arts|entertainment|arts and entertainment) events
ArtsAndEntertainmentCategoryIntent to (give|tell) me all the (events|occasions) (for|about) (arts|entertainment|arts and entertainment)
ClubsCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (a club meeting|club events)
ClubsCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (a club meeting|club events)
ClubsCategoryIntent (what is|what's) an (event|occasion) (for|about) club events
ClubsCategoryIntent I (want|need) all the club events
ClubsCategoryIntent an (event|occasion) (for|about) club events
ClubsCategoryIntent is (the|a) (for|about) (a club meeting|club events)
ClubsCategoryIntent to (give|tell) me a club meeting
GetEndTimeIntent (give|tell) me (the|a) time for {eventName}
GetEndTimeIntent (give|tell) me (what is|what's) (the|a) time for {eventName}
GetEndTimeIntent (what is|what's) (the|a) time for {eventName}
GetEndTimeIntent I (want|need) (the|a) time for {eventName}
GetEndTimeIntent a time for {eventName}
GetEndTimeIntent is (the|a) time for {eventName}
GetEndTimeIntent to (the|a) time for {eventName}
GetEventsOnDateIntent (give|tell) me (a|an|the) event (that is|that's) on {date}
GetEventsOnDateIntent (give|tell) me (what is|what's) (scheduled|happening) (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar (for|on) {date}
GetEventsOnDateIntent (give|tell) me (what is|what's) up (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar (for|on) {date}
GetEventsOnDateIntent (give|tell) me (what is|what's|what's up) on {date}
GetEventsOnDateIntent (what is|what's) on {date} (that is|that's) (fun|interesting|cool)
GetEventsOnDateIntent (what is|what's) on {date} for (fun|me to do|an activity)
GetEventsOnDateIntent I (want|need) (something|an event) to (do|attend) on {date}
GetEventsOnDateIntent I (want|need) a (gathering|fun time) on {date}
GetEventsOnDateIntent I (want|need) an (event|occasion) on {date}
GetFeeDetailsIntent (a|an) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent (give|tell) me (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent (to|for) (the|a) (price|cost|amount|fee|charge) for {eventName}
GetFeeDetailsIntent (|what is|what's) (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent I (want|need) (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent how much (does it cost|will I be charged|will I need to pay|will it cost|will I need) for {eventName}
GetFeeDetailsIntent is (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetLocationDetailsIntent (give|tell) me (the|a) (location|place) for {eventName}
GetLocationDetailsIntent (give|tell) me (what is|what's) (the|a) (location|place) for {eventName}
GetLocationDetailsIntent (what is|what's) (the|a) (location|place) for {eventName}
GetLocationDetailsIntent I (want|need) (the|a) (location|place) for {eventName}
GetLocationDetailsIntent a (location|place) for {eventName}
GetLocationDetailsIntent is (the|a) (location|place) for {eventName}
GetLocationDetailsIntent to (the|a) (location|place) for {eventName}
LecturesCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (a lecture|lectures)
LecturesCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (a lecture|lectures)
LecturesCategoryIntent (what is|what's) an (event|occasion) (for|about) lectures
LecturesCategoryIntent I (want|need) all the lectures
LecturesCategoryIntent an (event|occasion) (for|about) lectures
LecturesCategoryIntent is (the|a) (for|about) (a lecture|lectures)
LecturesCategoryIntent to (give|tell) me a lecture
NextEventIntent (give|tell) me (what is|what's) next to (do|see|go to) (that is|that's) (fun|interesting|cool)
NextEventIntent (give|tell) me the (next|most recent|upcoming|newest|latest) (event|thing|thing to do)
NextEventIntent (something|an event) (that is|that's) next
NextEventIntent (what is|what's) next
NextEventIntent (what is|what's) next (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar
NextEventIntent I (want|need) (a|the) next
NextEventIntent a (thing|thing to do) (that is|that's) next
NextEventIntent an (event|occasion) (that is|that's) next
NextEventIntent is (happening|scheduled) next (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar
NextEventIntent to (locate|find) (something|a thing|something to do|something fun) (that is|that's) (happening|scheduled) next
SportsCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent (what is|what's) an (event|occasion) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent I (want|need) all the (events|occasions) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent an (event|occasion) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent is (the|a) (for|about) (sports|althetics|intramural sports|intramural athletics) event
SportsCategoryIntent to (give|tell) me all the (events|occasions) (for|about) (sports|althetics|intramural sports|intramural athletics)
```
