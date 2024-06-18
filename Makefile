build:
	javac -g -d . -cp ./src/FrontEnd/ ./src/FrontEnd/*.java

clean:
	rm -f ./FrontEnd/*.class