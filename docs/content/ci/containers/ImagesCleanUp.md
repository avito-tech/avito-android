# Docker images clean up

For internal use we publish all images to [Avito internal harbor](http://links.k.avito.ru/images-harbor-speed). \
To reduce space we clean up all debug images after 30 days since the last `image-pull`. [Task with details IAAS-1347](http://links.k.avito.ru/IAAS-1347) 
