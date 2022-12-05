Diff algorithm is copy-pasted from [jgit](https://git.eclipse.org/r/plugins/gitiles/jgit/jgit/)
Last change: [1c886d92f6de1d577cbcc212e1e25b2c3207d5fb](https://git.eclipse.org/r/plugins/gitiles/jgit/jgit/+/1c886d92f6de1d577cbcc212e1e25b2c3207d5fb)  
Last change date: 16.11.2022

### How to use

1. Get instance `val algorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM)`
2. Implement `Sequence` and `SequenceComparator` for your data type (see `StringSequence` and `StringComparator` for example)
3. Generate diff by calling `algorithm.diff(sequenceComparator, sequenceA, sequenceB)`
4. Traverse resulting `EditList`
