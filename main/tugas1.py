import optparse
from pengcit.histogram import HistogramMaker

parser = optparse.OptionParser()

parser.add_option('-f', '--file', dest="filename", help="input image file name")

options, args = parser.parse_args()

options_dict = vars(options)
if 'filename' in options_dict:
    histogram_maker = HistogramMaker()
    histogram_maker.histogram_from_image(options.filename)
else:
    print("Please input your image filename using -f 'filename'")
    exit()
