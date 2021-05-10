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


def foo_b(cls, c, d):
    print("Method Relocation")


# update return type hint
def foo_c() -> int:
    print("Hello, Three!")
    return 23


# rename, public
def bar_d():
    print("Hello, Four!")


# insert parameter e with default value
def foo_e(e="Alan"):
    print("Hello, Five!")


# remove parameter f with default value
def foo_f():
    print("Hello, Six!")


# update parameter g to h with the same default value
def foo_g(h=2021):
    print("Hello, Seven!")


# add parameter default value
def foo_h(i="Yuhe"):
    print("Hello, Eight!")


# remove parameter default value
def foo_i(j):
    print("Hello, Nine!")


# update parameter default value same data type
def foo_j(k="YYY"):
    print("Hello, Ten!")
