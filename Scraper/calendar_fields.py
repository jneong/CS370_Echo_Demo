#!/usr/bin/env python2.7

"""
This module contains functions for fetching SSU event calendars and
extracting information from the data structures.

The file is structured such that the functions that deal with handling
the calendar info can be imported from a shell or another Python script,
or this module itself can be run as a standalone program.

As a standalone program, this script fetches the SSU event calendars in
iCalendar format, parses the calendars using the vobject library, analyzes
the data to answer questions of interest, and outputs the results in
Markdown format.

To generate the results, run the following command in a bash shell:

    ./calendar_fields.py > CalendarFields.md

There are two dependencies required for this script: requests and vobject.

Requests is a friendlier wrapper around the Python libraries for performing
HTTP requests.
http://docs.python-requests.org/en/master/

VObject is a versatile iCalendar library that came out of the Chandler project.
http://eventable.github.io/vobject/

Using a virtualenv for this project is recommended.  If you aren't familiar
with virtualenv, it is well worth taking a few moments to become acquainted
with.  Users of PyCharm will be glad to know that virtualenv is integrated into
the IDE.

The descriptions of Python in the comments of this file are supposed to only
reflect the behavior of Python 2.7. as that is what Amazon make available on
Lambda.  Python 3.x is generally similar but better in just about every way.
Prefer Python 3.x where possible.  Curses, Amazon.
"""

"""
The following third-party libraries are required to run this script.
They can be installed with pip:

pip install requests vobject

Alternatively, PyCharm has its own interface for installing Python packages.
"""
import requests
import vobject

# This string is a template, the {} will be replaced with the name of
# the file after formatting.  Alternatively, an array of the full URLs
# could have been used.
CALENDAR_URL_TEMPLATE = "https://25livepub.collegenet.com/calendars/{}.ics"

# An array of the filenames (without the .ics extension) for the calendars.
CALENDARS = [
    "Highlighted_Event",
    "Arts_and_Entertainment",
    "Athletics_New",
    "Club_and_Student_Organizations",
    "Community_and_Alumni",
    "Diversity_Related",
    "Lectures_and_Films",
    "Student_Calendar",
    "Classes_and_Workshops"
]


def fetch(name):
    """
    Perform an HTTP GET request to download the calendar specified
    by name.  If the response we get is OK (200), the response text
    is parsed into a calendar object using the vobject library and
    returned.  Otherwise, return None.
    """
    # It may seem weird to have a random string hanging out at the top
    # of a function definition.  In Python this is how you document code.
    # Python has a built-in help() function that will display the help-
    # string for whatever is passed.  For example: `help(fetch)` in an
    # interactive Python interpreter will display the above text.

    # The full URL is formatted from the URL template and
    # the name of the file.
    response = requests.get(CALENDAR_URL_TEMPLATE.format(name))

    if response.ok:
        # Server responded 200 (ok), we should have the requested
        # calendar file in the response text.
        # The response text is parsed into a calendar and returned.
        return vobject.readOne(response.text)
    else:
        # Something went wrong, return None (a null value).
        return None


def get_events(cal):
    """
    Given a calendar object (vobject returned by fetch()),
    return a list of the events contained in the calendar.
    """

    # The vobject calendar is a fairly complex container.  There are many
    # different types of children it can contain, one of which is VEVENT
    # (an artifact of the iCalendar format).  We can get a list of all
    # VEVENT children by accessing the .vevent_list property.
    # Using _list as a suffix is a common pattern in the VObject library.

    return cal.vevent_list


def get_basic_fields(event):
    """
    Return the names of the fields (properties) of a given event.
    """

    # An event has various fields or properties, such as when it starts,
    # when it ends, what it's called, where it is, etc.
    # The contents of an event is a dictionary of these properties, where
    # the lookup key is the name of the property.
    # In Python, dictionaries have a .keys() method that returns a list
    # of all the keys in the dictionary.
    # So to get the names of the fields, we return the keys of the contents.

    return event.contents.keys()


def make_value_getter(key):
    """
    Given a key, return a function that gets the values for that key,
    from an event passed to that function.
    """

    # This is a common pattern in Python: returning a nested function.
    # The function defined here uses the value for `key` passed in to
    # the outer function.

    def get_values(event):
        """
        Yield the values of the contents of a given event for a
        particular key.
        """

        # This function displays another Python technique, the
        # generator.  Generators behave like iterators, or lazy lists.
        # Instead of building a list of values and returning the list,
        # a generator yields one value at a time.  The yield statement
        # is like a return statement plus a bookmark, so the next
        # iteration resumes where the previous left off.
        # Generators are useful when processing lists, because they are
        # lazy; they only process one element at a time, so there is no
        # need to reserve memory for the all the results (in fact, you
        # can make infinite generators), and only as much processing as
        # is useful needs to be done (you don't have to go over the whole
        # list at once, and the calling context can decide to stop in the
        # middle of the list.

        # We aren't sure if the key we have exists in the event we are given,
        # so .get() is used to look up the value.  If the key does not exist,
        # the second parameter is returned instead (`()`, an empty tuple,
        # in this case).
        # The value of event.contents[key] is actually a list of all the values
        # for that field, since there can be multiple instances of the same
        # field in an event.
        # We iterate over the value found for the key, yielding each item from
        # the array of values.  In the case of an event where the key does not
        # exist, we end up iterating over an empty tuple, which yields nothing.

        # Note that the value of `key` from the outer function is used here.

        for content in event.contents.get(key, ()):
            for item in content.value:
                yield item

    # Remember we were defining an inner function inside an outer function?
    # We return the inner function from the outer function.  Effectively, we
    # have made a custom function at run-time based on the key.
    # Now we can make any number of functions using this same pattern for
    # different keys, simply by calling this outer function for each key.

    return get_values


# And here it is in action.  make_value_getter() returns a function,
# so we are creating a function to get the categories listed for an event,
# and another function to get the location listed for an event.
# These functions can be used like any function:
#   categories = get_categories(event)
#   locations = get_location(event)
get_categories = make_value_getter('categories')
get_location = make_value_getter('location')

# Note that get_location() is still a generator function, so it is actually
# creating an iterator that yields all the locations of an event.
# To get just one location (if we dare make that assumption) we can pull a
# single value from the iterator with the built in function, next():
#   location = next(locations)
# Or simply,
#   location = next(get_location(event))


# The "Calendars as a Service" service that SSU uses is based on a calendar
# server called Trumba.  Trumba provides users with a way to add custom fields
# to their calendars, using a custom iCalendar object type.
#
# All the custom fields are the same field type as far as the iCalendar format
# is concerned so we get them in a list of all the values for that type.  The
# custom fields are differentiated by params (parameters).  The field params
# can be accessed as a dictionary object, using a parameter name to obtain a
# list of the parameter's values.
#
# Trumba's custom field has several parameters, but the interesting ones here
# are ID, NAME, and TYPE.
# ID and NAME are alternative representations of the same information.
# TYPE is a sometimes misleading description of how the value for the custom
# field is encoded.  For example, 'SingleLine' would be a single line text field.
# 'Boolean' would be a True/False value.  But number actually can mean an
# enumeration, in other words a selection from a list of values.  In the case of
# the enumeration, we actually get the string value, not a numerical value.

def get_custom_fields(event):
    """
    Yield a tuple of (ID, NAME, TYPE) for all the custom fields
    in a given event.
    """

    # Another use of the *_list property on a vobject.
    # There can be multiple (different) custom fields, and we want
    # to look at all of them.
    for field in event.x_trumba_customfield_list:
        # The param values are a list, but we only need the first
        # (only) element here, hence the [0] subscripts below.

        # Field ID (id is already the name of a Python built-in)
        fid = int(field.params['ID'][0])

        # Field Name
        name = field.params['NAME'][0]

        # Field Type (type is another built-in)
        kind = field.params['TYPE'][0]

        # Yield all three values as a tuple.
        yield (fid, name, kind)


def make_custom_field_getter(match_fid):
    """
    Make a getter function for a specified field ID.
    """

    # This pattern should look a bit familiar now, it's another nested function.
    def get_custom_field(event):
        """
        Yield the values of a certain custom field for a given event.
        """
        for field in event.x_trumba_customfield_list:
            # We are filtering based on the custom field ID parameter,
            # so we'll get that.
            fid = int(field.params['ID'][0])

            if fid == int(match_fid):
                # When the ID matches the ID given in the outer function,
                # we spit out the value.
                yield field.value

            # The loop continues until the list is exhausted.

    # Returning the inner function we just created.
    return get_custom_field


# 'Event Type' is one of the custom fields we are interested in.
# The ID for the 'Event Type' field is 12.
#
# To drive the idea home, make_custom_field_getter() returns a function,
# so this statement is effectively the same as (apart from the scope issue):
#
# def get_custom_event_types(event):
#     return custom_field_getter(12)
#
# The naming convention of {verb}_foo_bar is another helpful reminder that
# get_custom_event_types is a function.
get_custom_event_types = make_custom_field_getter(12)

# Here we actually want to do some additional processing on the results from
# the getter, so we do wrap the getter in a conventional method definition.
def get_custom_categories(event):
    # But the same meta-function is still useful:
    getter = make_custom_field_getter(3138)

    for value in getter(event):
        # Here's the trick: The categories are listed in a single string as
        # comma separated values.  We can simply split the string everywhere
        # ', ' is found to get an array of all the categories in the list.
        for category in value.split(', '):
            yield category


# If you aren't yet familiar with map() and reduce(), they are staples of
# data processing.  If you've ever heard of Hadoop, that's a glorified way
# of doing map()/reduce().
#
# I'll introduce both concepts here for completeness, though only reduce is
# used at this point.
#
# map(func, sequence, ...) => applies func() to each element in the sequence,
#   and returns a list of the results.  See the help text for more details.
# Basically equivalent to:
#   def map(func, sequence):
#       return [func(elem) for elem in sequence]
#
# reduce(func, sequence) => applies func() to each element in the sequence and
#   the result of the previous iteration.  For the first cycle, the first two
#   elements from the sequence are used.  There is an optional initializer that
#   can be provided.  See the help text for more details.
#   Returns the final result as a single value.
# Basically equivalent to:
#   def reduce(func, sequence):
#       it = iter(sequence)
#       result = next(it)
#       for elem in it:
#           result = func(result, elem)
#       return result
#
# In other words, map() transforms one sequence into another sequence using a
# unary operator func(), while reduce() collapses a sequence into a single value
# using a binary operator func(). (unary:= one operand, binary:= two operands)

# Here we are creating a custom reducer function.
#
# Given some series of events, and a selector function that selects values we are
# interested in from an event, we want to find the set of unique values obtained
# from all the events.
#
# To do so, we gather the values from each event using selector(), then obtain the
# set of those results (set in the math sense, where each discrete value occurs at
# most once within the set) using the Python built-in set() function. When we have
# collected a set of items for each event, we find the union of all the sets, using
# the reduce() built-in to apply the set.union() operator across every set, and
# return the result.
#
# The actual implementation is pleasantly concise:

def reduce_events(events, selector):
    """
    Returns the union of the sets of items selected from events by selector.
    """
    return reduce(set.union, [set(selector(event)) for event in events])


# One thing worth mentioning is the use of the list comprehension.
# For those not familiar with Python, a list comprehension is a compact
# way to write a simple for loop.  In the above code, the list comprehension is:
#   [set(selector(event)) for event in events]
# This is roughly equivalent to:
#   result = []
#   for event in events:
#       result.append( set( selector(event) ) )
# There are also dictionary comprehensions, generator comprehensions, which might
# have made more sense here, and set comprehensions, which sound like they could
# be used above given all the set operations we were doing, but probably wouldn't
# actually be very useful for the specific task at hand.
#
# Anyway, comprehensions are cool, so get used to them.


def count_iter(it):
    """
    Returns the number of iterations before the iterator stopped.
    Don't pass an infinite iterator ;)
    """
    # _ is a valid variable identifier, conventionally used to indicate a
    # throwaway value.
    # sum() is a python built-in that returns the sum of the numbers in an
    # iterable.
    # Python interprets the code in the place of the parameter list here as
    # a generator comprehension.
    # This method is a neat trick I found for counting the number of elements
    # in an iterator (len() doesn't work for whatever reason).
    # The comprehension yields a 1 for every element it encounters in the
    # iterator, and sum adds them up to get the count.

    return sum(1 for _ in it)


def events_have_multiple_values(events, selector):
    """
    Test whether multiple values are encountered by the selector for any
    event in events.  Returns True if multiple values are found, otherwise
    False.
    """
    # any() is a Python built-in that returns True if any item in the iterable
    # given can be interpreted as a True value.
    # Here we see another implicit generator comprehension for the iterable
    # parameter.
    return any(count_iter(selector(event)) > 1 for event in events)


# And that's it!  Those are all the functions that actually deal with the calendar.
# The rest of this is the Python version of main(), for when this module is run as
# a standalone script.
# When this module is imported, the code below is not executed.

if __name__ == "__main__":

    # When this module runs as a script, we want it to collect some interesting
    # information from the calendars and print the result in Markdown format so
    # it can be pasted into a GitHub wiki page.

    # Just a function for +, which is a binary operator (operates on two values).
    from operator import add

    # Combine all the events from all the calendars into one flat list of events.
    # The list comprehension gets the lists of events from each calendar,
    # the `reduce(add,` part collapses the list of lists into one list by adding
    # them all together (adding lists in Python creates a joined list).

    events = reduce(add, [get_events(fetch(c)) for c in CALENDARS])

    # This little function definition uses the events collected above, and is just
    # a helper gadget to save some typing and make things readable.
    # sorted() is another built-in, its function is pretty self-explanatory.

    def reducer(selector): return sorted(reduce_events(events, selector))

    # Some helpers to emit Markdown
    def heading(text):
        print
        print "### {}".format(text)
    def bullet(text): print "* {}".format(text)
    def italic(text): print "*{}*".format(text)

    def bullet_list(it): map(bullet, it) # There's that map() built-in :)
    def bullets(selector): bullet_list(reducer(selector)) # The magic happens here!

    def section(text, selector):
        heading(text)
        bullets(selector)

    def note_multiple_values(selector, yes, no):
        print
        italic(yes if events_have_multiple_values(events, selector) else no)

    section("Basic Fields", get_basic_fields)

    section("Categories", get_categories)
    note_multiple_values(
        get_categories,
        "some events are tagged with multiple categories",
        "each event has only one category"
    )

    heading("Recurrence")
    # The vobject library parses recurrence rules into the rruleset property.
    if all(e.rruleset == None for e in events):
        italic("there are no events with recurrence rules")
    else:
        italic("there are some events with recurrence rules")

    # We won't waste space printing out all the locations.  Just a count is fine.
    heading("Locations")
    print "count = %d" % count_iter(reducer(get_location))
    if all(e.contents.get('location', None) != None for e in events):
        italic("all events have a location")
    else:
        italic("there are some events without a location")

    # We do some special text formatting here with the tuples.
    heading("Custom Fields (id, name, type)")
    bullet_list(
        "({}, {}, {})".format(fid, name, kind)
        for fid, name, kind in reducer(get_custom_fields)
    )

    section("Custom Event Types", get_custom_event_types)
    print
    if all(True for e in events
           if any(True for f in get_custom_fields(e)
                  if f[1] == 'Event Type')):
        italic("all events have an Event Type")
    else:
        italic("there are some events without an Event Type")

    section("Custom Categories", get_custom_categories)
    note_multiple_values(
        get_custom_categories,
        "some events are tagged with multiple custom categories",
        "each event has only one custom category"
    )
