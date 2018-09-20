import os
from flask import Flask, request, redirect, url_for, render_template, jsonify
from werkzeug import secure_filename
import base64
from image_thinning import ImageThinner, convert_image2bw
from pylab import *
from PIL import Image

UPLOAD_FOLDER = 'uploads/'
ALLOWED_EXTENSIONS = set(['jpg', 'png', 'gif'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

@app.route('/')
def index():
  return render_template('template.html', label='', imagesource='../uploads/template.jpg' )

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route('/image_thinning', methods=['GET', 'POST'])
def image_thinning():
  if request.method == 'POST':
    file = request.files['file']

    if file and allowed_file(file.filename):
      filename = secure_filename(file.filename)

      file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
      file.save(file_path)

      image_thinner = ImageThinner()
      result = image_thinner.thin_image(convert_image2bw(array(Image.open(file_path).convert('L'))))

      result = Image.fromarray(result)

      result_filepath = 'uploads/' + 'thinning_result.png'
      result.save(result_filepath)

      return render_template('template.html', imageresult='../' + result_filepath, imagesource='../uploads/' + filename)

  else:
    return render_template('template.html', imageresult='', imagesource='../uploads/template.jpg' )


@app.route('/image_thinning_base64', methods=['POST'])
def image_thinning_base64():
  if request.method == 'POST':
    data = request.get_json()

    if data is None:
      print('No valid response')
      return jsonify({'error': 'No valid request body, json is missing!'})
    else:
      img_data = data['img']
      filename = data['img_filename']

      convert_base64_image(img_data, filename)
      file_path = 'uploads/' + filename

      image_thinner = ImageThinner()
      result = image_thinner.thin_image(convert_image2bw(array(Image.open(file_path).convert('L'))))

      result = Image.fromarray(result)

      result_filepath = 'uploads/' + 'thinning_result.png'
      result.save(result_filepath)

      image = open(result_filepath, 'rb').read()
      image_64encode = base64.b64encode(image)

      return jsonify({'result': str(image_64encode)})

@app.route('/predict_digit', methods=['GET', 'POST'])
def predict_digit():
  if request.method == 'POST':
    file = request.files['file']

    if file and allowed_file(file.filename):
      filename = secure_filename(file.filename)

      file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
      file.save(file_path)

      return render_template('template.html', imageresult='', imagesource='../uploads/' + filename)

  else:
    return render_template('template.html', imageresult='', imagesource='../uploads/template.jpg' )

@app.route('/predict_digit_base64', methods=['POST'])
def predict_digit_base64():
  if request.method == 'POST':
    data = request.get_json()

    if data is None:
      print('No valid response')
      return jsonify({'error': 'No valid request body, json is missing!'})
    else:
      img_data = data['img']
      filename = data['img_filename']

      convert_base64_image(img_data, filename)

      return jsonify({'success': 'upload success'})

def convert_base64_image(bs64_string, filename):
  with open('uploads/' + filename, 'wb') as f:
    f.write(base64.decodebytes(bs64_string.encode()))

from flask import send_from_directory

@app.route('/uploads/<filename>')
def uploaded_file(filename):
  return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

if __name__ == "__main__":
  app.run()