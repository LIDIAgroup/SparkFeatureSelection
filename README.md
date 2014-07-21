SparkFeatureSelection
=====================

This package contains a generic implementation of greedy Information Theoretic Feature Selection methods. The impelemntation is based on the common theoretic framework presented in [1]. Implementations of mRMR, InfoGain, and other commonly used FS filters are provided. In addition, the framework can be extended with user provided criteria as long as the process complies with the framework proposed in [1].
The framework also also contains an Entropy Minimization Discretization implementation [2] in order to treat non discrete datasets as well as two artificial dataset generators to test current and new feature selection criteria.


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
