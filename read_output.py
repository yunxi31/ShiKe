import sys

try:
    with open('test_output.txt', 'r', encoding='utf-16') as f:
        content = f.read()
    # Print only non-XML lines or lines containing SUCCESS/FAILURE/Clicking/Typing
    for line in content.splitlines():
        if not line.strip().startswith("<?xml") and not line.strip().startswith("<hierarchy"):
            print(line)
except Exception as e:
    print("Error:", e)
