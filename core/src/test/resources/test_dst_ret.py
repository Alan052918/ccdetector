# add return type hint
def foo_a() -> int:
    print("Hello, One!")
    return 12


# remove return type hint
def foo_b():
    print("Hello, Two!")
    return "String"


# update return type hint
def foo_c() -> int:
    print("Hello, Three!")
    return 23