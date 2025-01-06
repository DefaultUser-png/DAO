# DAO
### A Java tool for bulk renames
> [!WARNING]
> Requiers Java 21 to run!

> *(Please ignore this repository, I'm just a burnout student setting this up in the middle of the night, because we were informed way too late that our projects must be hosted here)*\
[Demo images](https://drive.google.com/file/d/1XCcSyWzwJSYPPDeLWwUb410eHQTy5XIV/view?usp=drivesdk)\
[Plan B](https://drive.google.com/file/d/1XDRulsv_hAyP3AXSy6yUhQdfqyjIWGGA/view?usp=drivesdk)

DAO (Greek: δώστε άλλο όνομα, "give another name") is an utility to quickly rename multiple files at once. The program comes with a varierty of QOL features, such as
* ⌨️​ Text keys that can add custom indexing and timestamps into the file names;
* ​✏️​ An option to change name and extension separataly;
* ​✔️ Previews for all selected files;
* 📤​ Accesability via context menu (currently executed through "Send to" menu because I couldn't code a shell extension);
* 📁 File sorting;
* ↩️​ An ability to undo changes;
* 📝​ Console/File logs;
> *(Basically a PowerRename clone because at the moment of coding we didn't know it exists)*

### Text keys
DAO offers a set of keys that could be used when renaming files:
| Key | Description | Preview |
| --- | --- | --- |
| `$<>` | Adds a counter to the name | `file0, file1, file2, ...` |
| `$<increment=5>` | Adds a counter to the name with a specified increment | `file0, file5, file10, ...` |
| `$<fixed=3>` | Adds a counter to the name with a specified number of digits | `file000, file001, file002, ...` |
| `$<start=7>` | Adds a counter to the name that starts with a specified value | `file7, file8, file9, ...` |
| `$<increment=5 fixed=3 start=7>` | Adds a counter to the name with specified properties | `file007, file012, file017, ...` |
| `$<yMdHms_->` | Replaces letters "yMdHms" with a formatted creation date that is seperated by "_" or "-", if included | `$<yy> -> file_25`<br/>`$<yyyy> -> file_2025`<br/>`$<yyyyMMdd_HH-mm-ss> -> file_20250102_16-26-08` |
| `$<randb=5>` | Returns a sequence of random letters (A-Za-z) and digits | `file_gId62, file_9odH0, ...` |
| `$<randint=5>` | Returns a sequence of random digits | `file_43286, file_96546, ...` |
| `$<randstr=5>` | Returns a sequence of random letters (A-Za-z) | `file_JkdNP, file_AqmDK, ...` |
