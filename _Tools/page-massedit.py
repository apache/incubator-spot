import os
import fnmatch
import massedit
import re


print('\nWelcome to the regex page editor')
print('Use this to make changes across all the Spot pages fast (mainly menus)')
print('This is a result of laziness; enjoy \n')


def FindFiles(_rootDir, _pattern: str):
    _fileList = []

    for root, dirs, files in os.walk(_rootDir):
        for file in fnmatch.filter(files, _pattern):
            _fileList.append(root + "\\" + file)

    return _fileList


def RegSearch(_searchPattern, _files):
    filteredFiles = []

    for filePath in _files:
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


def RegDelLines(_searchPattern, _files):
    for filePath in _files:
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


def RegInsertAfter(_searchPattern, _files, _line2Insert, _insertType):
    for filePath in _files:
        baseName = os.path.basename(filePath)
        newFileName = os.path.dirname(filePath) + "\\" + baseName + ".new"

        with open(filePath, "r", encoding='utf-8') as oldFile:
            # create swap file
            with open(newFileName, "w", encoding='utf-8') as newFile:
                for line in oldFile:
                    pattern = re.compile(_searchPattern)
                    matches = pattern.findall(line)

                    newFile.write(line)

                    if len(matches) > 0:
                        newFile.write(_line2Insert)


        # Swap Files
        os.rename(filePath, filePath + ".old")
        os.rename(newFileName, filePath)
        os.remove(filePath + ".old")


rootDir = "c:\\code\\incubator-spot"  #input('enter directory:')

files = FindFiles(rootDir, 'index.html')

searchPattern = input("Search Pattern:")

files = RegSearch(searchPattern, files)

print("commands(DelLines / InsertAfter/ ...)")

while True:
    cmd = input("How to Proceed?: ")

    # Delete Lines
    if cmd.lower() == 'dellines':
        RegDelLines(searchPattern, files)

    # Insert After
    elif cmd.lower() == 'insertafter':
        insertType = input('append (app) or next line (nl)')

        # normalise
        if insertType in ['app', 'append']:
            insertType = 'a'
        elif insertType in ['next line', 'nl']:
            insertType = 'n'

        if insertType in ['a', 'n']:
            line2Insert = input("What to insert: ")
            RegInsertAfter(searchPattern, files, line2Insert, insertType)
        else:
            print("Invalid Insertion Type")

    # Regex Replace
    elif cmd == 'regex':
        print("Not Implemented Yet")
        #massedit.edit_files(files, ["re.sub()"])

    # Quit
    elif cmd in ['q', 'quit']:
        break

    # ?!?
    else:
        print("Invalid Command")

print('Bye')
