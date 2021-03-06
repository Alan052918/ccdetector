# ccdetector

A static analysis tool depending on [GumTree](https://github.com/GumTreeDiff/gumtree) to detect 14 types of source code refactoring changes.

This is my undergraduate thesis project, please access the thesis report at [Automatic Detection of Python API Changes](https://github.com/Alan052918/Undergraduate-Graduation-Project-Thesis/blob/master/main.pdf).

## Supported Changes

### Method renaming

Adding or removing the leading underscore in Python method names effects their accessibility, while `def foo():` is public, `def _foo():` is weakly private.

- public to weakly private
- weakly private to public
- no accessibility switch (normal update)

### Method Relocation

- Method insertion
- Method removal
- Method relocation

### Parameter Changes

- Parameter insertion
- Parameter removal
- Parameter update
  - normal update
  - cls/self switch

### Parameter Default Value Changes

- Parameter default value addition
- Parameter default value removal
- Parameter default value update

### Return Type Changes

- Return type hint addition
- Return type hint removal
- Return type hint update

## Test Cases

Currently only [core](core) is implemented, test cases in [core/src/test/resources](core/src/test/resources).

- Method renaming test cases: [core/src/test/resources/test_src_mren.py](core/src/test/resources/test_src_mren.py) & [core/src/test/resources/test_dst_mren.py](core/src/test/resources/test_dst_mren.py)
- Method relocation test cases: [core/src/test/resources/test_src_mrel.py](core/src/test/resources/test_src_mrel.py) & [core/src/test/resources/test_dst_mrel.py](core/src/test/resources/test_dst_mrel.py)
- Parameter changes & Parameter default value changes test cases: [core/src/test/resources/test_src_param.py](core/src/test/resources/test_src_param.py) & [core/src/test/resources/test_dst_param.py](core/src/test/resources/test_dst_param.py)
- Return type changes test cases: [core/src/test/resources/test_src_ret.py](core/src/test/resources/test_src_ret.py) & [core/src/test/resources/test_dst_ret.py](core/src/test/resources/test_dst_ret.py)
