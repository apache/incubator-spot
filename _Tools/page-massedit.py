import os
import fnmatch
import massedit
import re


print('Welcome to the regex page editor')
print('Use this to make changes across all the Spot pages (mainly menus) fast')
print('This is a result of laziness; enjoy')


def FindFiles(_rootDir, pattern: str):
    _fileList = []

    for root, dirs, files in os.walk(_rootDir):
        for file in fnmatch.filter(files, pattern):
            _fileList.append(root + "\\" + file)

    return _fileList


def RegSearch(_searchPattern, files):
    filteredFiles = []

    for filePath in files:
        with open(filePath, "r", encoding='utf-8') as file:
            for line in file:
                pattern = re.compile(_searchPattern)
                matches = pattern.findall(line)
                if len(matches) > 0:
                    print("{} in file {}".format(line, filePath))
                    filteredFiles.append(filePath)
                    break

    print("\n{} files found containing {} \n\n".format(len(filteredFiles), _searchPattern))
    return filteredFiles


def RegDelLines(_searchPattern, files):
    for filePath in files:
        baseName = os.path.basename(filePath)
        newFileName = os.path.dirname(filePath) + "\\" + baseName + ".new"

        with open(filePath, "r", encoding='utf-8') as oldFile:
            # create swap file
            with open(newFileName, "w", encoding='utf-8') as newFile:
                for line in oldFile:
                    pattern = re.compile(_searchPattern)
                    matches = pattern.findall(line)
                    if len(matches) == 0:
                        newFile.write(line)
                    else:
                        print("skip")

        # Swap Files
        os.rename(filePath, filePath + ".old")
        os.rename(newFileName, filePath)
        os.remove(filePath + ".old")


rootDir = "c:\\code\\incubator-spot"  #input('enter directory:')

files = FindFiles(rootDir, 'index.html')

searchPattern = "slack|SLACK|Slack" #input("Search Pattern:")

files = RegSearch(searchPattern, files)

print("commands(DelLines / ...)")
cmd = input("How to Proceed?: ")

if cmd.lower() == 'dellines':
    RegDelLines(searchPattern, files)
elif cmd == 'regex':
    print("Not Implemented Yet")
    #massedit.edit_files(files, ["re.sub()"])

print('Done')