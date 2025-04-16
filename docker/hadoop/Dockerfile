FROM apache/hadoop:3.3.4

# Installer les dépendances supplémentaires
RUN apt-get update && apt-get install -y --no-install-recommends \
    openssh-server \
    && rm -rf /var/lib/apt/lists/*

# Configuration SSH
RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    chmod 0600 ~/.ssh/authorized_keys

# Exposer les ports
EXPOSE 9000 9870 9864 9866 9867

# Configuration d'environnement
ENV HDFS_NAMENODE_USER=root
ENV HDFS_DATANODE_USER=root
ENV HDFS_SECONDARYNAMENODE_USER=root
ENV YARN_RESOURCEMANAGER_USER=root
ENV YARN_NODEMANAGER_USER=root

# Point d'entrée
CMD ["sh", "-c", "service ssh start && /opt/hadoop/bin/hdfs namenode -format && /opt/hadoop/sbin/start-dfs.sh && tail -f /dev/null"]