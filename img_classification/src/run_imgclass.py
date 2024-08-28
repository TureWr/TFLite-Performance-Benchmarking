import os
import pathlib
import sys
import tensorflow as tf
from pycoral.utils import dataset
from pycoral.adapters import common, classify
from PIL import Image
import time

def run_inference(processing_unit):
    script_dir = pathlib.Path(__file__).parent.absolute()

    # Specify the model file
    if processing_unit == 'tpu':
        model_file = os.path.join(script_dir, 'models/tf2_mobilenet_v2_1.0_224_ptq_edgetpu.tflite')
    else:
        model_file = os.path.join(script_dir, 'models/tf2_mobilenet_v2_1.0_224_ptq.tflite')
    
    label_file = os.path.join(script_dir, 'imagenet_labels.txt')
    labels = dataset.read_label_file(label_file)

    image_folder = os.path.join(script_dir, 'images')
    image_paths = [os.path.join(image_folder, f) for f in os.listdir(image_folder) if f.lower().endswith(('.jpg', '.jpeg', '.png', '.webp'))]
    
    # Initialize the interpreter
    if processing_unit == 'tpu':
        from pycoral.utils import edgetpu
        interpreter = edgetpu.make_interpreter(model_file)
        interpreter.allocate_tensors()
    else:
        interpreter = tf.lite.Interpreter(model_path=model_file)
        interpreter.allocate_tensors()

    total_time = 0
    total_images = 0

    message = "Processing images with {}"

    for image_path in image_paths:
        # Resize the image
        size = common.input_size(interpreter)
        image = Image.open(image_path).convert('RGB').resize(size, Image.LANCZOS)
        
        # Run an inference and calculate the processing time
        common.set_input(interpreter, image)
        start_time = time.monotonic()
        
        if processing_unit == 'cpu':
            with tf.device('/cpu:0'):
                message = message.format('cpu')
                interpreter.invoke()
        elif processing_unit == 'gpu':
            if tf.config.experimental.list_physical_devices('GPU'):
                with tf.device('/gpu:0'):
                    message = message.format('gpu')
                    interpreter.invoke()
            else:
                message = message.format('cpu')
                with tf.device('/cpu:0'):
                    interpreter.invoke()
        else:
            message = message.format('tpu')
            interpreter.invoke()
        
        total_time += time.monotonic() - start_time
        
        classes = classify.get_classes(interpreter, top_k=1)
        total_images += 1
        
        image_name = image_path.split('/')[-1].split('.')[0].lower()
        predicted_label = labels.get(classes[0].id, '').lower()

        print(f"Predicted label: {predicted_label}")

    print(message)
    if total_images > 0:
        print(f"Average time per image: {total_time / total_images}")
    else:
        print("No images processed")

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Please provide an argument to specify the processing unit (tpu, cpu, gpu)")
        sys.exit(1)
    processing_unit = sys.argv[1]
    if processing_unit not in ['tpu', 'cpu', 'gpu']:
        print("Please provide a valid processing unit (tpu, cpu, gpu)")
        sys.exit(1)
    run_inference(processing_unit)
