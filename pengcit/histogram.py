from PIL import Image
from pylab import array, imshow
import matplotlib.pyplot as plt

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
        for pixels in pil_image_array:
            for pixel in pixels:
                red = pixel[0]
                green = pixel[1]
                blue = pixel[2]
                grayscale = (int(red) + int(green) + int(blue)) // 3 # convert to int for overflow handler
                
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
        
        return ((red_dict, green_dict, blue_dict, grayscale_dict))

    def show_im(self, pil_im):
        imshow(pil_im)

    def plot_rgb_grayscale(self, value_dict, color_name, histogram_color):
        '''
        Input: value_dict = dictionary of quantity for each color value, color_name = Color name for title, histogram_color = color for histogram
        Return a histogram image
        '''
        width = 1.0
        plt.bar(value_dict.keys(), value_dict.values(), color=histogram_color)
        plt.title("Histogram of %s Color in Image" % color_name)
        plt.xlabel('Color Value')
        plt.ylabel('Quantity')