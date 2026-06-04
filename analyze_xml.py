import xml.etree.ElementTree as ET
import sys

# Write a script to dump and parse all nodes with any text or content-desc
xml_str = open("window_dump_test.xml", "r", encoding="utf-8").read()
try:
    root = ET.fromstring(xml_str)
    for node in root.iter('node'):
        text = node.attrib.get('text', '')
        desc = node.attrib.get('content-desc', '')
        cls = node.attrib.get('class', '')
        bounds = node.attrib.get('bounds', '')
        if text or desc:
            print(f"Class: {cls} | Text: {text} | Desc: {desc} | Bounds: {bounds}")
except Exception as e:
    print("Error:", e)
