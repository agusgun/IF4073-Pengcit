from PIL import Image
from pylab import array, imshow, figure, show
import matplotlib.pyplot as plt
import numpy as np

class HistogramMaker:
    def __init__(self):
        return

    def get_rgb_grayscale_dict(self, pil_image_array):
        '''
        Input: Pil Image Array
        Return tuple of r, g, b, and grayscale dict
        '''
        red_dict = dict()
        green_dict = dict()
        blue_dict = dict()
        grayscale_dict = dict()
        arrs = []
        arr = []
        print(pil_image_array)
        for pixels in pil_image_array:
            arr = []
            for pixel in pixels:
                red = pixel[0]
                green = pixel[1]
                blue = pixel[2]
                grayscale = (int(red) + int(green) + int(blue)) // 3 # convert to int for overflow handler
                if grayscale > 127:
                    grayscale = 0
                else:
                    grayscale = 1
                arr.append(grayscale)
                # Check Red
                if red not in red_dict:
                    red_dict[red] = 1
                else:
                    red_dict[red] += 1

                # Check Green
                if green not in green_dict:
                    green_dict[green] = 1
                else:
                    green_dict[green] += 1

                # Check Blue
                if blue not in blue_dict:
                    blue_dict[blue] = 1
                else:
                    blue_dict[blue] += 1

                if grayscale not in grayscale_dict:
                    grayscale_dict[grayscale] = 1
                else:
                    grayscale_dict[grayscale] += 1

            arrs.append(arr)
        f= open("out.txt","w+")
        for i in arrs:
            print(i)
            for j in i:
                f.write(str(j))
            f.write('\n')
        f.close()
        return ((red_dict, green_dict, blue_dict, grayscale_dict))

    def show_image(self, pil_im, image_filename):
        imshow(pil_im)
        plt.title(image_filename)
        plt.axis('off')

    def plot_histogram(self, value_dict, color_name, histogram_color):
        '''
        Input: value_dict = dictionary of quantity for each color value, color_name = Color name for title, histogram_color = color for histogram
        Return a histogram image
        '''
        width = 1.0
        plt.bar(value_dict.keys(), value_dict.values(), color=histogram_color, width=2)
        plt.title("Histogram of %s Color in Image" % color_name)
        plt.xlabel('Color Value')
        plt.ylabel('Quantity')
        plt.xlim(-5, 260)
        plt.grid(True)

    def histogram_from_image(self, image_filename):
        pil_im = array(Image.open(image_filename))

        red_dict, green_dict, blue_dict, grayscale_dict = self.get_rgb_grayscale_dict(pil_im)
        # print(red_dict)
        # plt.figure(figsize=(20,10))
        # plt.subplot(4, 1, 1)
        # self.show_image(pil_im, image_filename)
        #
        # plt.subplot(4, 2, 3)
        # self.plot_histogram(red_dict, 'Red', 'r')
        #
        # plt.subplot(4, 2, 4)
        # self.plot_histogram(green_dict, 'Green', 'g')
        #
        # plt.subplot(4, 2, 7)
        # self.plot_histogram(blue_dict, 'Blue', 'b')
        #
        # plt.subplot(4, 2, 8)
        # self.plot_histogram(grayscale_dict, 'Grayscale', 'k')
        #
        # show()

histogram_maker = HistogramMaker()
histogram_maker.histogram_from_image("X.bmp")
