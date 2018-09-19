from flask import Flask, request, redirect, url_for

app = Flask(__name__)

@app.route('/')
def index():
  return ("Yo, it's working!")

@app.route('/predict_digit', methods=['GET', 'POST'])
def predict_digit():
  if request.methods='POST':
    print('YAHAI')
  else:
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
  if request.methods='POST':
    print('YAHAI')
  else:
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