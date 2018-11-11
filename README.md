# edge2panda
An Android application for pix2pix with tensorflow mobile. 

## Demo
![https://github.com/wzc118/edge2panda/blob/master/edge2pandas.gif]


## Model transformer
We use the tensorflow mobile for mobile application. There are some tricks for model transformation.
* Reduce the pix2pix model, by only saving Generator.
```Python
python pix2pix_android.py --model-input <raw model input folder> --model-output <reduced model output folder>
```
* freeze model
```Python
python freeze_model.py --model-folder <reduced model output folder>
```
* Quantization by using bazel
```Python
bazel-bin/tensorflow/tools/graph_transforms/transform_graph \
--in_graph=<forzen model folder>/frozen_model.pb \
--out_graph=<forzen model folder>/frozen_model_android_quantized.pb --inputs='image_tensor' \
--outputs='generator/deprocess/div' \
--transforms='quantize_weights'
```
* Last
Put the quantized model to the assest folder in project.
