import os
from flask import Flask, request, redirect, url_for

UPLOAD_FOLDER = '/tmp/'
ALLOWED_EXTENSIONS = set(['jpg', 'png', 'gif'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

@app.route('/')
def index():
  return ("Yo, it's working!")

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS


@app.route('/predict_digit', methods=['GET', 'POST'])
def predict_digit():
  if request.method == 'POST':
    print('YAHAI')
  return """
  <!doctype html>
  <title>Upload new File</title>
  <h1>Upload new File</h1>
  <form action="" method=post enctype=multipart/form-data>
    <p><input type=file name=file>
       <input type=submit value=Upload>
  </form>
  <p>%s</p>
    """ % "<br>".join(os.listdir(app.config['UPLOAD_FOLDER'],))

@app.route('/image_thinning', methods=['GET', 'POST'])
def image_thinning():
  if request.method == 'POST':
    file = request.files['file']
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return redirect(url_for('index'))
  return """
  <!doctype html>
  <title>Upload new File</title>
  <h1>Upload new File</h1>
  <form action="" method=post enctype=multipart/form-data>
    <p><input type=file name=file>
       <input type=submit value=Upload>
  </form>
  <p>%s</p>
  """ % "<br>".join(os.listdir(app.config['UPLOAD_FOLDER'],))  

if __name__ == "__main__":
  app.run()