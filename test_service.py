from random import choice

from flask import Flask, jsonify, request


results = ["PASSED", "FAILED", "ERROR", "UNKNOWN"]


app = Flask(__name__)


@app.route('/test', methods=['GET', 'POST'])
def index():
    print request.json
    return jsonify({"status": choice(results)})


if __name__ == '__main__':
    app.run(debug=True, port=8787)

