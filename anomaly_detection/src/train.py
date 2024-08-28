import os
import numpy as np
import pandas as pd
import time
import sys
import tensorflow as tf
import tflite_runtime.interpreter as tflite

from utilities import select_gpus, plot_results, slide_window
from tcnae import TCNAE
import data

# Retrieve the number of epochs, with a default value
epochs = 1  # Default number of epochs
if len(sys.argv) > 1:
    try:
        epochs = int(sys.argv[1])
    except ValueError:
        print("Warning: Invalid number of epochs provided. Using default value of 1.")

train_ts_id = 1
data_gen = data.Data()
train_data = data_gen.build_data(train_ts_id, verbose=0)
train_X = train_data["train_X"]
print("train_X.shape:", train_X.shape)

test_ts_id = 3
test_data = data_gen.build_data(test_ts_id, verbose=0)
test_X = test_data["scaled_series"].values[np.newaxis, :, :] # Adding a new axis for the batch size

# save test data in a separate array file for later use
np.save(f"../dataMG/test_data{test_ts_id}.npy", test_X)
print(test_X.shape)


# save the anomaly labels for the test data in a separate array file

anomaly_labels = test_data["is_anomaly"]
np.save(f"../dataMG/anomaly_labels{test_ts_id}.npy", anomaly_labels)

# Try to load existing model
model_path = f"models/tcnae{epochs}_.tflite"

print("Training a new model...")
# Build and compile the model
tcn_ae = TCNAE()
tcn_ae.fit(train_X, train_X, batch_size=32, epochs=epochs, verbose=1)

# Save the model as a TensorFlow Lite model with post-training quantization

# Convert the mode
converter = tf.lite.TFLiteConverter.from_keras_model(tcn_ae.model)
converter.target_spec.supported_ops=[
    tf.lite.OpsSet.TFLITE_BUILTINS,
    tf.lite.OpsSet.SELECT_TF_OPS
    ]
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

# Save the model
with open(model_path, 'wb') as f:
    f.write(tflite_model)

print(f"Model saved as {model_path}")


# Perform prediction using the newly trained model
start_time = time.time()
anomaly_score = tcn_ae.model.predict(test_X)
print("> Time for new model:", round(time.time() - start_time), "seconds.")
print("Anomaly score (new model):", anomaly_score)

# Plot results
plot_results(test_data, anomaly_score, pl_range=None, plot_signal=False, plot_anomaly_score=True)
plot_results(test_data, anomaly_score, pl_range=(40000, 42000), plot_signal=True, plot_anomaly_score=False)

print("Done")
