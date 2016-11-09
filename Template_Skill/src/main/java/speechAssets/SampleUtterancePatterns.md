These patterns are for use with the Intent Utterance Expander, found here:
https://lab.miguelmota.com/intent-utterance-expander/example/

Documentation for utterances can be found here:
https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/supported-phrases-to-begin-a-conversation

````
GetEventsOnDateIntent (give|tell) me (what is|what's) (scheduled|happening) (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar (for|on) {date}
GetEventsOnDateIntent (give|tell) me (what is|what's) up (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar (for|on) {date} 
GetEventsOnDateIntent (give|tell) me (a|an|the) event (that is|that's) on {date}
GetEventsOnDateIntent (give|tell) me (what is|what's|what's up) on {date}
GetEventsOnDateIntent I (want|need) an (event|occasion) on {date} 
GetEventsOnDateIntent I (want|need) a (gathering|fun time) on {date}
GetEventsOnDateIntent I (want|need) (something|an event) to (do|attend) on {date}
GetEventsOnDateIntent (what is|what's) on {date} (that is|that's) (fun|interesting|cool)
GetEventsOnDateIntent (what is|what's) on {date} for (fun|me to do|an activity)
GetFeeDetailsIntent (give|tell) me (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent how much (does it cost|will I be charged|will I need to pay|will it cost|will I need) for {eventName} 
GetFeeDetailsIntent is (the|a) (price|cost|amount|fee|charge) (for|about) {eventName} 
GetFeeDetailsIntent (to|for) (the|a) (price|cost|amount|fee|charge) for {eventName}
GetFeeDetailsIntent I (want|need) (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent (a|an) (price|cost|amount|fee|charge) (for|about) {eventName}
GetFeeDetailsIntent (|what is|what's) (the|a) (price|cost|amount|fee|charge) (for|about) {eventName}
GetLocationDetailsIntent (give|tell) me (the|a) (location|place) for {eventName}
GetLocationDetailsIntent (give|tell) me (what is|what's) (the|a) (location|place) for {eventName}
GetLocationDetailsIntent is (the|a) (location|place) for {eventName}
GetLocationDetailsIntent to (the|a) (location|place) for {eventName}
GetLocationDetailsIntent I (want|need) (the|a) (location|place) for {eventName}
GetLocationDetailsIntent a (location|place) for {eventName}
GetLocationDetailsIntent (what is|what's) (the|a) (location|place) for {eventName}
GetEndTimeIntent
AllCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) 
AllCategoryIntent (give|tell) me (what is|what's) up
AllCategoryIntent is (the|a) (scheduled|happening)
AllCategoryIntent to (give|tell) me all the (events|occasions)
AllCategoryIntent I (want|need) all the (events|occasions)
AllCategoryIntent an (event|occasion)
AllCategoryIntent (what is|what's) an (event|occasion)
SportsCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent is (the|a) (for|about) (sports|althetics|intramural sports|intramural athletics) event  
SportsCategoryIntent to (give|tell) me all the (events|occasions) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent I (want|need) all the (events|occasions) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent an (event|occasion) (for|about) (sports|althetics|intramural sports|intramural athletics)
SportsCategoryIntent (what is|what's) an (event|occasion) (for|about) (sports|althetics|intramural sports|intramural athletics)
ArtsAndEntertainmentCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent is (the|a) (for|about) (arts|entertainment|arts and entertainment) events  
ArtsAndEntertainmentCategoryIntent to (give|tell) me all the (events|occasions) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent I (want|need) all the (events|occasions) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent an (event|occasion) (for|about) (arts|entertainment|arts and entertainment)
ArtsAndEntertainmentCategoryIntent (what is|what's) an (event|occasion) (for|about) (arts|entertainment|arts and entertainment)
LecturesCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (a lecture|lectures)
LecturesCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (a lecture|lectures)
LecturesCategoryIntent is (the|a) (for|about) (a lecture|lectures)  
LecturesCategoryIntent to (give|tell) me a lecture
LecturesCategoryIntent I (want|need) all the lectures
LecturesCategoryIntent an (event|occasion) (for|about) lectures
LecturesCategoryIntent (what is|what's) an (event|occasion) (for|about) lectures
ClubsCategoryIntent (give|tell) me (what is|what's) (scheduled|happening) (for|about) (a club meeting|club events)
ClubsCategoryIntent (give|tell) me (what is|what's) up (for|about|with) (a club meeting|club events)
ClubsCategoryIntent is (the|a) (for|about) (a club meeting|club events)  
ClubsCategoryIntent to (give|tell) me a club meeting
ClubsCategoryIntent I (want|need) all the club events
ClubsCategoryIntent an (event|occasion) (for|about) club events
ClubsCategoryIntent (what is|what's) an (event|occasion) (for|about) club events
NextEventIntent (give|tell) me the (next|most recent|upcoming|newest|latest) (event|thing|thing to do)
NextEventIntent (give|tell) me (what is|what's) next to (do|see|go to) (that is|that's) (fun|interesting|cool)
NextEventIntent is (happening|scheduled) next (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar
NextEventIntent to (locate|find) (something|a thing|something to do|something fun) (that is|that's) (happening|scheduled) next
NextEventIntent I (want|need) (a|the) next  
NextEventIntent an (event|occasion) (that is|that's) next
NextEventIntent a (thing|thing to do) (that is|that's) next
NextEventIntent (what is|what's) next (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar
NextEventIntent (what is|what's) next
NextEventIntent (something|an event) (that is|that's) next
```