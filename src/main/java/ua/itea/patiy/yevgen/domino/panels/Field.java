package ua.itea.patiy.yevgen.domino.panels;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import org.netbeans.lib.awtextra.AbsoluteConstraints;

import ua.itea.patiy.yevgen.domino.engine.Bone;
import ua.itea.patiy.yevgen.domino.engine.Game;

public final class Field extends GamePanel {
	private static final long serialVersionUID = 8185038222875756793L;
	private int xLine;
	private int yLine;
	private int xCenter;
	private int yCenter;
	private int fieldWidth;
	private int fieldHeight;
	private int spaceLeft;
	private int spaceRight;
	private int spaceUp;
	private int spaceDown;
	private Random randomizer = new Random();
	private boolean isTurnTopLeft;
	private boolean isTurnTopRight;
	private boolean isTurnBottomLeft;
	private boolean isTurnBottomRight;
	private boolean isRandomTurn = randomizer.nextBoolean();

	public Field(String enemyName) {
		setTitle(" Це ігрове поле. Для початку беріть з базара 7 каменів. Те ж саме зробить і супротивник " + enemyName
				+ " ");
	}

	protected Bone leftBone() { // левый камень на панели
		return getBones().get(0);
	}

	protected Bone rightBone() { // правый камень на панели
		return getBones().get(getBones().size() - 1);
	}

	public void selectFieldBones(Player player, Bone bone) { // Выбираем камень на поле
		setSelectedLeft(null);
		setSelectedRight(null);

		player.disableBonesSelect();
		for (Bone fieldBone : getBones()) {
			if ((!fieldBone.equals(bone) & (fieldBone.isSelected()))) {
				fieldBone.toggleBoneSelection();
			} else if (fieldBone.equals(bone)) {
				fieldBone.toggleBoneSelection();
			}

			if (fieldBone.isSelected()) {
				if (leftBone().equals(fieldBone)) {
					setSelectedLeft(fieldBone);
				} else if (rightBone().equals(fieldBone)) {
					setSelectedRight(fieldBone);
				}
			}
		}
		player.enableBonesSelect(getSelectedLeft(), getSelectedRight());
		repaint();
	}

	@Override
	protected void rebuildBonesLine(boolean frame) { // цепляем мышку для левого и правого камней, перерисовываем рамку
		getBones().forEach(bone -> {
			bone.removeMouseListener(bone.clickOnField); // убираем обработку мыши и рамку для всех камней
			if (bone.isSelected()) {
				bone.unselect(); // рисуем с нормальной мордой
			}
			bone.hideFrame();
		});
	}

	public void disableBonesSelect() {
		rebuildBonesLine(Game.NOFRAME);
	}

	public void enableFieldSelect(Player player) {
		if (getBones().size() == 1) { // если одна кость на поле, цепляем к ней мышку
			Bone temp = leftBone();

			for (Bone bone : player.getBones()) {
				if ((bone.isBoneGoodToMove(temp.getLeft())) || (bone.isBoneGoodToMove(temp.getRight()))) { // если хоть
																											// один
																											// камень
					// игрока подходит, разрешаем
					// щелкать по первому камню
					// на поле
					temp.addMouseListener(temp.clickOnField);
					temp.showFrame();
					break;
				}
			}
			getBones().set(0, temp);
		} else {
			getBones().forEach(bone -> {
				bone.removeMouseListener(bone.clickOnField); // убираем обработку мыши и рамку для всех камней
				bone.hideFrame();
			});

			Bone temp = leftBone(); // к левой
			for (Bone bone : player.getBones()) {
				if (bone.isBoneGoodToMove(temp.getWorkSide())) { // если хоть один камень игрока подходит, разрешаем
																	// щелкать по
					// левому камню на поле
					temp.addMouseListener(temp.clickOnField);
					temp.showFrame();
					break;
				}
			}
			getBones().set(0, temp);

			temp = rightBone(); // к правой
			for (Bone bone : player.getBones()) {
				if (bone.isBoneGoodToMove(temp.getWorkSide())) { // если хоть один камень игрока подходит, разрешаем
																	// щелкать по
					// левому камню на поле
					temp.addMouseListener(temp.clickOnField);
					temp.showFrame();
					break;
				}
			}
			getBones().set(getBones().size() - 1, temp);
		}
	}

	private void putAtPosition(boolean where, Bone bone, int x, int y) {
		bone.removeMouseListener(bone.clickOnHumanPlayer);
		if (where == Game.TORIGHT) {
			getBones().add(bone); // даем камень справа
		} else if (where == Game.TOLEFT) {
			getBones().add(0, bone); // даем камень слева
		}

		bone.showBone();
		bone.setLocation(x, y);
		add(bone, new AbsoluteConstraints(x, y, bone.getWidth(), bone.getHeight()));
		repaint();
	}

	public void addFirstBone(Bone bone) {
		int angle = Game.Angle.A0.getAngle();
		if (bone.isDuplet()) {
			angle = Game.Angle.A90.getAngle();
			bone.setWorkSide(bone.getRight());
		}
		bone.draw(angle, Game.UNSELECTED);

		fieldWidth = this.getWidth();
		fieldHeight = this.getHeight();

		xCenter = fieldWidth / 2;
		yCenter = fieldHeight / 2;

		xLine = xCenter - bone.getWidth() / 2;
		yLine = yCenter - bone.getHeight() / 2;

		bone.setFirst(true);
		putAtPosition(Game.TORIGHT, bone, xLine, yLine);

		spaceLeft = (fieldWidth - bone.getWidth()) / 2; // свободное пространство слева
		spaceRight = (fieldWidth - bone.getWidth()) / 2; // свободное пространство справа

		spaceUp = (fieldHeight - bone.getHeight()) / 2; // свободное пространство сверху
		spaceDown = (fieldHeight - bone.getHeight()) / 2; // свободное пространство снизу

		isTurnTopLeft = false;
		isTurnTopRight = false;
		isTurnBottomLeft = false;
		isTurnBottomRight = false;
	}

	private void addRightToLeft(Bone previous, Bone bone) {
		int angle = Game.Angle.A0.getAngle(); // если просто камень, горизонтально

		boolean turnFromHorizontalDuplet = (previous.isDuplet() == true)
				&& ((previous.getAngle() == Game.Angle.A0.getAngle())
						|| (previous.getAngle() == Game.Angle.A180.getAngle()));
		boolean turnFromVerticalBone = (previous.isDuplet() == false)
				&& ((previous.getAngle() == Game.Angle.A90.getAngle())
						|| (previous.getAngle() == Game.Angle.A270.getAngle()));
		boolean prevVerticalDuplet = (previous.isDuplet() == true)
				&& ((previous.getAngle() == Game.Angle.A90.getAngle())
						|| (previous.getAngle() == Game.Angle.A270.getAngle()));
		boolean prevHorizontalBone = (previous.isDuplet() == false)
				&& ((previous.getAngle() == Game.Angle.A0.getAngle())
						|| (previous.getAngle() == Game.Angle.A180.getAngle()));

		if (bone.isDuplet() == false) {
			if (previous.getWorkSide() == bone.getLeft()) {
				angle += 180; // переворачиваем камень наоборот
			}
		} else {
			angle += 90; // если дупль
		}

		bone.draw(angle, Game.UNSELECTED); // отрисовываем

		xLine = previous.getX() - bone.getWidth() - Game.OFFSET;

		if ((prevVerticalDuplet) || (prevHorizontalBone) || (turnFromHorizontalDuplet)) {
			yLine = previous.getY() + (previous.getHeight() / 2) - (bone.getHeight() / 2);
		}

		if (turnFromVerticalBone) {
			if (isTurnTopRight == true) {
				if (bone.isDuplet() == false) {
					yLine = previous.getY();
				} else {
					yLine = previous.getY() - (bone.getHeight() / 2) - (Game.OFFSET / 2);
				}
				isTurnTopRight = false;
			}
			if (isTurnBottomRight == true) {
				if (bone.isDuplet() == false) {
					yLine = previous.getY() + previous.getHeight() - bone.getHeight();
				} else {
					yLine = previous.getY() + previous.getHeight() - (bone.getHeight() / 2);
				}
				isTurnBottomRight = false;
			}
		}

		bone.setWorkSide(bone.getLeft()); // рабочая часть камня левая

		if (getBones().size() == 1) { // в начале игры ставим камень слева
			putAtPosition(Game.TOLEFT, bone, xLine, yLine);
		} else {
			if (previous.equals(leftBone())) { // если работаем с левым концом, ставим камень слева
				putAtPosition(Game.TOLEFT, bone, xLine, yLine);
			} else {
				putAtPosition(Game.TORIGHT, bone, xLine, yLine); // если с правым то справа
			}
		}
		spaceLeft -= bone.getWidth();
	}

	private void addLeftToRight(Bone previous, Bone bone) {
		int angle = Game.Angle.A0.getAngle(); // если просто камень, горизонтально

		boolean turnFromHorizontalDuplet = (previous.isDuplet() == true)
				&& ((previous.getAngle() == Game.Angle.A0.getAngle())
						|| (previous.getAngle() == Game.Angle.A180.getAngle()));
		boolean turnFromVerticalBone = (previous.isDuplet() == false)
				&& ((previous.getAngle() == Game.Angle.A90.getAngle())
						|| (previous.getAngle() == Game.Angle.A270.getAngle()));
		boolean prevVerticalDuplet = (previous.isDuplet() == true)
				&& ((previous.getAngle() == Game.Angle.A90.getAngle())
						|| (previous.getAngle() == Game.Angle.A270.getAngle()));
		boolean prevHorizontalBone = (previous.isDuplet() == false)
				&& ((previous.getAngle() == Game.Angle.A0.getAngle())
						|| (previous.getAngle() == Game.Angle.A180.getAngle()));

		if (bone.isDuplet() == false) {
			if (previous.getWorkSide() == bone.getRight()) {
				angle = Game.Angle.A180.getAngle(); // переворачиваем камень наоборот
			}
		} else {
			angle = Game.Angle.A90.getAngle(); // если дупль
		}

		bone.draw(angle, Game.UNSELECTED); // отрисовываем

		xLine = previous.getX() + previous.getWidth() + Game.OFFSET;

		if ((prevVerticalDuplet) || (prevHorizontalBone) || (turnFromHorizontalDuplet)) {
			yLine = previous.getY() + (previous.getHeight() / 2) - (bone.getHeight() / 2);
		}

		if (turnFromVerticalBone) {
			if (isTurnTopLeft == true) {
				if (bone.isDuplet() == false) {
					yLine = previous.getY();
				} else {
					yLine = previous.getY() - (bone.getHeight() / 2) - Game.OFFSET;
				}
				isTurnTopLeft = false;
			}
			if (isTurnBottomLeft == true) {
				if (bone.isDuplet() == false) {
					yLine = previous.getY() + previous.getHeight() - bone.getHeight();
				} else {
					yLine = previous.getY() + previous.getHeight() - (bone.getHeight() / 2);
				}
				isTurnBottomLeft = false;
			}
		}

		bone.setWorkSide(bone.getRight()); // рабочая часть камня правая

		if (getBones().size() == 1) { // в начале игры ставим камень справа
			putAtPosition(Game.TORIGHT, bone, xLine, yLine);
		} else {
			if (previous.equals(leftBone())) { // если работаем с левым концом, ставим камень слева
				putAtPosition(Game.TOLEFT, bone, xLine, yLine);
			} else {
				putAtPosition(Game.TORIGHT, bone, xLine, yLine); // если с правым то справа
			}
		}
		spaceRight -= bone.getWidth();
	}

	private void addDownToUp(Bone previous, Bone bone) {
		int angle = Game.Angle.A90.getAngle(); // переворачиваем на 90

		boolean turnFromHorizontalBone = (previous.isDuplet() == false)
				&& ((previous.getAngle() == Game.Angle.A0.getAngle())
						|| (previous.getAngle() == Game.Angle.A180.getAngle())); // крайний камень по
		// горизонтали
		boolean turnFromHorizontalDuplet = (previous.isDuplet() == true)
				&& ((previous.getAngle() == Game.Angle.A90.getAngle())
						|| (previous.getAngle() == Game.Angle.A270.getAngle())); // крайний дупль по
		// горизонтали
		boolean prevVerticalBone = (previous.isDuplet() == false) && ((previous.getAngle() == Game.Angle.A90.getAngle())
				|| (previous.getAngle() == Game.Angle.A270.getAngle())); // предыдущий камень по
		// вертикали
		boolean prevVerticalDuplet = (previous.isDuplet() == true) && ((previous.getAngle() == Game.Angle.A0.getAngle())
				|| (previous.getAngle() == Game.Angle.A180.getAngle())); // предыдущий дупль по
		// вертикали

		if (bone.isDuplet() == false) {
			if ((previous.getRight() == bone.getLeft()) || (previous.getLeft() == bone.getLeft())) {
				angle = Game.Angle.A270.getAngle(); // переворачиваем камень наоборот
			}
		} else {
			angle = Game.Angle.A0.getAngle(); // если дупль
		}
		bone.draw(angle, Game.UNSELECTED); // отрисовываем

		yLine = previous.getY() - bone.getHeight() - Game.OFFSET;

		if (previous.equals(rightBone())) { // если работаем с правым концом

			if (turnFromHorizontalBone) { // от не дупля по горизонтали поворачиваем вертикально
				xLine = previous.getX() + previous.getWidth() / 2 + Game.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (bone.isDuplet() == false)) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX() + (previous.getWidth() / 2) - (bone.getWidth() / 2);
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (bone.getWidth() / 2);
			}
			bone.setWorkSide(bone.getLeft());
			putAtPosition(Game.TORIGHT, bone, xLine, yLine);
		}

		if (previous.equals(leftBone())) { // если работаем с левым концом

			if ((turnFromHorizontalBone) && (bone.isDuplet() == false)) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим не дупль
				xLine = previous.getX();
			}

			if ((turnFromHorizontalBone) && (bone.isDuplet() == true)) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим дупль
				xLine = previous.getX() - (bone.getWidth() / 2) - Game.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (bone.isDuplet() == false)) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX();
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (bone.getWidth() / 2);
			}

			bone.setWorkSide(bone.getLeft());
			putAtPosition(Game.TOLEFT, bone, xLine, yLine);
		}
		spaceUp -= bone.getHeight();
	}

	private void addUpToDown(Bone previous, Bone bone) {
		int angle = Game.Angle.A90.getAngle(); // переворачиваем на 90

		boolean turnFromHorizontalBone = (previous.isDuplet() == false)
				&& ((previous.getAngle() == Game.Angle.A0.getAngle())
						|| (previous.getAngle() == Game.Angle.A180.getAngle())); // крайний камень по
		// горизонтали
		boolean turnFromHorizontalDuplet = (previous.isDuplet() == true)
				&& ((previous.getAngle() == Game.Angle.A90.getAngle())
						|| (previous.getAngle() == Game.Angle.A270.getAngle())); // крайний дупль по
		// горизонтали
		boolean prevVerticalBone = (previous.isDuplet() == false) && ((previous.getAngle() == Game.Angle.A90.getAngle())
				|| (previous.getAngle() == Game.Angle.A270.getAngle())); // предыдущий камень по
		// вертикали
		boolean prevVerticalDuplet = (previous.isDuplet() == true) && ((previous.getAngle() == Game.Angle.A0.getAngle())
				|| (previous.getAngle() == Game.Angle.A180.getAngle())); // предыдущий дупль по
		// вертикали

		if (bone.isDuplet() == false) {
			if ((previous.getRight() == bone.getRight()) || (previous.getLeft() == bone.getRight())) {
				angle = Game.Angle.A270.getAngle(); // переворачиваем камень наоборот
			}
		} else {
			angle = Game.Angle.A0.getAngle(); // если дупль
		}
		bone.draw(angle, Game.UNSELECTED); // отрисовываем

		yLine = previous.getY() + previous.getHeight() + Game.OFFSET;

		if (previous.equals(rightBone())) { // если работаем с правым концом

			if (turnFromHorizontalBone) { // от не дупля по горизонтали поворачиваем вертикально
				xLine = previous.getX() + previous.getWidth() / 2 + Game.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (bone.isDuplet() == false)) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX() + (previous.getWidth() / 2) - (bone.getWidth() / 2);
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (bone.getWidth() / 2);
			}

			bone.setWorkSide(bone.getRight());
			putAtPosition(Game.TORIGHT, bone, xLine, yLine);
		}

		if (previous.equals(leftBone())) { // если работаем с левым концом

			if ((turnFromHorizontalBone) && (bone.isDuplet() == false)) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим не дупль
				xLine = previous.getX();
			}

			if ((turnFromHorizontalBone) && (bone.isDuplet() == true)) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим дупль
				xLine = previous.getX() - (bone.getWidth() / 2) - Game.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (bone.isDuplet() == false)) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX();
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (bone.getWidth() / 2);
			}

			bone.setWorkSide(bone.getRight());
			putAtPosition(Game.TOLEFT, bone, xLine, yLine);
		}
		spaceDown -= bone.getHeight();

	}

	public void addToLeft(Bone bone) {
		if (spaceLeft > Game.SPACELIMIT) {
			addRightToLeft(leftBone(), bone); // справа налево
		} else {
			if (isRandomTurn) {
				if (spaceUp > Game.SPACELIMIT) {
					addDownToUp(leftBone(), bone); // снизу вверх
				} else {
					isTurnTopLeft = true;
					addLeftToRight(leftBone(), bone); // слева направо
				}
			} else {
				if (spaceDown > Game.SPACELIMIT) {
					addUpToDown(leftBone(), bone); // сверху вниз
				} else {
					isTurnBottomLeft = true;
					addLeftToRight(leftBone(), bone); // слева направо
				}
			}
		}
	}

	public void addToRight(Bone bone) {
		if (spaceRight > Game.SPACELIMIT) {
			addLeftToRight(rightBone(), bone);
		} else {
			if (!isRandomTurn) {
				if (spaceUp > Game.SPACELIMIT) {
					addDownToUp(rightBone(), bone);
				} else {
					isTurnTopRight = true;
					addRightToLeft(rightBone(), bone);
				}
			} else {
				if (spaceDown > Game.SPACELIMIT) {
					addUpToDown(rightBone(), bone);
				} else {
					isTurnBottomRight = true;
					addRightToLeft(rightBone(), bone);
				}
			}
		}
	}

	@Override
	public void setTitle(String title) {
		this.setBorder(BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font("Dialog", 1, 12), new Color(255, 255, 255)));
	}

	@Override
	protected void addToBones(Bone bone) {
	}
}
