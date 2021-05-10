def foo_b(cls, c, d):
    print("Method Relocation")


def foo_a(self, a, b):
    for i in range(1, 100):
        output = ""
        p = False
        if i % 3 == 0:
            p = True
            output += "Fizz"
        if i % 5 == 0:
            p = True
            output += "Buzz"
        if p is True:
            print(output)
        else:
            print(i)


# update return type hint
def foo_c() -> str:
    print("Hello, Three!")
    return "Fool"


# rename, public
def foo_d():
    print("Hello, Four!")


# insert parameter e with default value
def foo_e():
    print("Hello, Five!")


# remove parameter f with default value
def foo_f(f=23):
    print("Hello, Six!")


# update parameter g to h with the same default value
def foo_g(g=2021):
    print("Hello, Seven!")


# add parameter default value
def foo_h(i):
    print("Hello, Eight!")


# remove parameter default value
def foo_i(j={1, "Good"}):
    print("Hello, Nine!")


# update parameter default value same data type
def foo_j(k="XXX"):
    print("Hello, Ten!")
