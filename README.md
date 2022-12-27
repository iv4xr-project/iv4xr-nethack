## Python directory
Install:
```bash
sudo apt install gcov lcov
```

Then in PyCharm in venv to install package
```
cd server-python
pip install -e .
```

Run [coverage](https://gcovr.com/en/stable/):
```
mkdir coverage -p
gcovr -r . --html-details -o coverage/example.html
```

Clear up coverage:
```
rm -rf *.gcda
```
