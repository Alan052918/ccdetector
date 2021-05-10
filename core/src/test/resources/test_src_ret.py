# add return type hint
def foo_a():
    print("Hello, One!")
    return 12


# remove return type hint
def foo_b() -> str:
    print("Hello, Two!")
    return "String"


# update return type hint
def foo_c() -> str:
    print("Hello, Three!")
    return "Fool"