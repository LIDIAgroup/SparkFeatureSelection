SparkFeatureSelection
=====================

The present framework implements feature selection on Spark for its application on Big Data problems. The framework is interoperable with the MLlib Spark library and will be part of it in the close future.
This package contains a generic implementation of greedy Information Theoretic Feature Selection methods. The implementation is based on the common theoretic framework presented in [1]. Implementations of mRMR, InfoGain, JMI and other commonly used FS filters are provided. In addition, the framework can be extended with other criteria provided by the user as long as the process complies with the framework proposed in [1].
The framework also contains an implementation of Entropy Minimization Discretization [2] in order to treat non discrete datasets as well as two artificial dataset generators to test current and new feature selection criteria.


## Contributors

- Héctor Mouriño-Talín (h.mtalin@udc.es)
- David Martínez-Rego (dmartinez@udc.es)

## References
```
[1] Brown, G., Pocock, A., Zhao, M. J., & Luján, M. (2012). 
"Conditional likelihood maximisation: a unifying framework for information theoretic feature selection." 
The Journal of Machine Learning Research, 13(1), 27-66.
```
```
[2] Fayyad, U., & Irani, K. (1993).
"Multi-interval discretization of continuous-valued attributes for classification learning."
```
